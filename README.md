## Visits and Tokens

ClinicQ supports patient registration, daily token generation, receptionist queue viewing, and public token tracking.

### Authentication

- Patient registration and today's queue require a valid JWT.
- Public token tracking does not require authentication.

### API Endpoints

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| POST | `/api/visits` | Register a patient and generate a queue token | Required |
| GET | `/api/visits/today?doctorId=1` | View today's queue for a doctor | Required |
| GET | `/api/public/visits/{publicId}` | View token status and patients ahead | Not required |

### Register a Patient

Send a `POST` request to `/api/visits` with a valid JWT.

Example request body:

```json
{
  "name": "Arun Kumar",
  "phone": "9876543210",
  "doctorId": 1
}
```

A successful registration returns the token number, patient name, doctor name, visit date, status, and public tracking ID.

### Public Token Tracking

Use the returned `publicId` to check a patient's token:

```http
GET /api/public/visits/{publicId}
```

The response includes the token number, doctor name, current status, and number of patients ahead.

### Queue Rules

- Each doctor has an independent token sequence.
- Token numbering starts at 1 for each doctor each day.
- The application uses the `Asia/Kolkata` timezone to determine the visit date.
- A unique public ID is generated for each visit.
- Database constraints and doctor-row locking help protect token numbering during concurrent registrations.