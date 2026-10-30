# ClinicQ — Smart Clinic Queue Management System

ClinicQ is a live token and queue management system designed for small clinics. Patients can track their queue position and estimated waiting time on their phones, while receptionists register patients and doctors manage consultations.

## Features

### Patient
- Track queue status using a personal tracking link.
- View token number and queue position.
- View estimated waiting time when available.
- Receive live queue updates without repeatedly refreshing the page.

### Receptionist
- Register patients for a selected doctor.
- View patient registration details.
- Generate a QR code for the patient's tracking page.
- View today's queue and patient statuses.

### Doctor
- View the current patient and waiting list.
- Call the next patient.
- Skip a patient when necessary.
- Mark a consultation as completed.

### Application
- Staff authentication with role-based page access.
- React frontend with responsive styling.
- Spring Boot backend.
- PostgreSQL database.
- Redis for queue-related functionality.
- WebSocket/STOMP integration for live queue updates.
- Docker Compose for local infrastructure.

## Tech Stack

| Technology | Purpose |
|---|---|
| React | Frontend user interface |
| Vite | Frontend development server and build tool |
| Tailwind CSS | Responsive styling |
| React Router | Frontend routing |
| Java | Backend programming language |
| Spring Boot | Backend application and REST APIs |
| Spring Security | Authentication and authorization |
| JWT | Staff authentication tokens |
| PostgreSQL | Relational database |
| Redis | Queue-related data and operations |
| WebSocket / STOMP | Real-time queue updates |
| Docker Compose | Local service setup |
| Maven | Backend dependency and build management |
| Git and GitHub | Version control and project hosting |

## Project Structure

```text
clinicq/
├── backend/                 # Spring Boot backend
├── frontend/                # React frontend
│   ├── public/              # Public assets and favicon
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/           # Application