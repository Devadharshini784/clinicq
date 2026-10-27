## Queue Control (Doctor)

The doctor can manage the patient queue through these endpoints:

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/doctor/queue` | View the current queue and today's visits |
| POST | `/api/doctor/queue/next` | Finish the current patient and call the next |
| POST | `/api/doctor/queue/skip` | Mark the current patient as skipped and call the next |
| POST | `/api/doctor/queue/done` | Finish the current consultation |

### Queue and Waiting-Time Estimates

- Patients receive a token number for their selected doctor and visit date.
- The queue is maintained in Redis for fast access.
- PostgreSQL remains the source of truth for patient visits.
- If Redis queue data is lost, the queue can be rebuilt from PostgreSQL.
- Estimated waiting time is calculated using the number of patients ahead and the doctor's average consultation duration.
- The default consultation duration is 5 minutes.
- The average is based on the doctor's last 10 completed consultations, when sufficient duration data is available.

### Roles and Permissions

- **ADMIN:** Manage doctors and perform administrative operations.
- **RECEPTIONIST:** Register patients and view today's visits.
- **DOCTOR:** View and control the doctor's own queue.

### Local Development Accounts

These development accounts are seeded automatically:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `reception` | `reception123` | RECEPTIONIST |
| `doctor` | `doctor123` | DOCTOR |

**Security note:** These credentials are for local development only. Change them before deploying the application.