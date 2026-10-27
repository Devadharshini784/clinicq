package com.clinicq.backend.queue;

import com.clinicq.backend.visit.Visit;
import com.clinicq.backend.visit.VisitRepository;
import com.clinicq.backend.visit.VisitStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class QueueService {

    private final StringRedisTemplate redis;
    private final VisitRepository visitRepository;
    private final Clock clock;
    private final int defaultConsultationSeconds;

    public QueueService(
            StringRedisTemplate redis,
            VisitRepository visitRepository,
            Clock clock,
            @Value("${app.queue.default-consultation-seconds}") int defaultConsultationSeconds) {
        this.redis = redis;
        this.visitRepository = visitRepository;
        this.clock = clock;
        this.defaultConsultationSeconds = defaultConsultationSeconds;
    }

    private String queueKey(Long doctorId, LocalDate date) {
        return "clinicq:queue:" + doctorId + ":" + date;
    }

    private String currentKey(Long doctorId, LocalDate date) {
        return "clinicq:current:" + doctorId + ":" + date;
    }

    private String durationsKey(Long doctorId) {
        return "clinicq:durations:" + doctorId;
    }

    private String initializedKey(Long doctorId, LocalDate date) {
        return "clinicq:init:" + doctorId + ":" + date;
    }

    public void addWaiting(Long doctorId, LocalDate date, int token) {
        ensureInitialized(doctorId, date);
        redis.opsForZSet().add(queueKey(doctorId, date), String.valueOf(token), token);
    }

    public void setCurrent(Long doctorId, LocalDate date, int token) {
        ensureInitialized(doctorId, date);
        redis.opsForValue().set(currentKey(doctorId, date), String.valueOf(token));
        redis.opsForZSet().remove(queueKey(doctorId, date), String.valueOf(token));
    }

    public void clearCurrent(Long doctorId, LocalDate date) {
        redis.delete(currentKey(doctorId, date));
    }

    public void recordDuration(Long doctorId, Instant startedAt, Instant finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return;
        }

        long seconds = Duration.between(startedAt, finishedAt).getSeconds();

        if (seconds < 20) {
            return;
        }

        seconds = Math.min(seconds, 3600);

        String key = durationsKey(doctorId);
        redis.opsForList().rightPush(key, String.valueOf(seconds));
        redis.opsForList().trim(key, -10, -1);
    }

    public QueueSnapshot snapshotToday(Long doctorId) {
        return snapshot(doctorId, LocalDate.now(clock));
    }

    public QueueSnapshot snapshot(Long doctorId, LocalDate date) {
        ensureInitialized(doctorId, date);

        List<Integer> waiting = new ArrayList<>();
        java.util.Set<String> values = redis.opsForZSet()
                .range(queueKey(doctorId, date), 0, -1);

        if (values != null) {
            for (String value : values) {
                waiting.add(Integer.parseInt(value));
            }
        }

        String currentValue = redis.opsForValue().get(currentKey(doctorId, date));
        Integer currentToken = currentValue == null ? null : Integer.valueOf(currentValue);

        return new QueueSnapshot(
                doctorId,
                date,
                currentToken,
                waiting,
                averageConsultationSeconds(doctorId)
        );
    }

    private int averageConsultationSeconds(Long doctorId) {
        List<String> durations = redis.opsForList().range(durationsKey(doctorId), 0, -1);

        if (durations == null || durations.isEmpty()) {
            return defaultConsultationSeconds;
        }

        long total = 0;
        for (String duration : durations) {
            total += Long.parseLong(duration);
        }

        return (int) (total / durations.size());
    }

    private void ensureInitialized(Long doctorId, LocalDate date) {
        String marker = initializedKey(doctorId, date);

        if (Boolean.TRUE.equals(redis.hasKey(marker))) {
            return;
        }

        rebuildFromDatabase(doctorId, date);
    }

    public void rebuildFromDatabase(Long doctorId, LocalDate date) {
        String queue = queueKey(doctorId, date);
        String current = currentKey(doctorId, date);
        String marker = initializedKey(doctorId, date);

        redis.delete(queue);
        redis.delete(current);

        List<Visit> visits = visitRepository
                .findByDoctorIdAndVisitDateOrderByTokenNumberAsc(doctorId, date);

        for (Visit visit : visits) {
            if (visit.getStatus() == VisitStatus.WAITING) {
                redis.opsForZSet().add(
                        queue,
                        String.valueOf(visit.getTokenNumber()),
                        visit.getTokenNumber()
                );
            } else if (visit.getStatus() == VisitStatus.IN_PROGRESS) {
                redis.opsForValue().set(current, String.valueOf(visit.getTokenNumber()));
            }
        }

        String durations = durationsKey(doctorId);

        if (!Boolean.TRUE.equals(redis.hasKey(durations))) {
            List<Visit> completed = visitRepository
                    .findTop10ByDoctorIdAndStatusOrderByFinishedAtDesc(doctorId, VisitStatus.DONE);

            for (int i = completed.size() - 1; i >= 0; i--) {
                Visit visit = completed.get(i);

                if (visit.getStartedAt() == null || visit.getFinishedAt() == null) {
                    continue;
                }

                long seconds = Duration.between(
                        visit.getStartedAt(),
                        visit.getFinishedAt()
                ).getSeconds();

                if (seconds >= 20) {
                    redis.opsForList().rightPush(
                            durations,
                            String.valueOf(Math.min(seconds, 3600))
                    );
                }
            }

            redis.opsForList().trim(durations, -10, -1);
        }

        redis.opsForValue().set(marker, "1");
    }
}