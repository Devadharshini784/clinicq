package com.clinicq.backend.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueueBroadcaster {

    private static final Logger log =
            LoggerFactory.getLogger(QueueBroadcaster.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final QueueService queueService;

    public QueueBroadcaster(
            SimpMessagingTemplate messagingTemplate,
            QueueService queueService) {
        this.messagingTemplate = messagingTemplate;
        this.queueService = queueService;
    }

    public void broadcast(Long doctorId) {
        try {
            QueueSnapshot snapshot = queueService.snapshotToday(doctorId);

            messagingTemplate.convertAndSend(
                    "/topic/queue/" + doctorId,
                    snapshot
            );
        } catch (Exception e) {
            log.warn("Could not broadcast queue for doctor {}", doctorId, e);
        }
    }
}
