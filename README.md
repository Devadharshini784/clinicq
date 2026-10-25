\# ClinicQ



Live token and queue system for small clinics.



Patients can see their position in the queue and their estimated waiting time on their own phone.



\## Status



Phase 2: Backend foundation and Doctor API completed.



\## Run Locally



\### Prerequisites



\- Java 17

\- Docker Desktop

\- IntelliJ IDEA

\- Postman (for API testing)



\### 1. Start PostgreSQL and Redis



From the project root, run:



```bash

docker compose up -d

```



\### 2. Run the backend



Open the `backend` folder in IntelliJ and run `BackendApplication.java`.



\### 3. Check backend health



Open:



http://localhost:8080/actuator/health



Expected response:



```json

{"status":"UP"}

```



\## Doctor API



Base URL: `http://localhost:8080/api/doctors`



| Method | Endpoint | Description |

|---|---|---|

| POST | `/api/doctors` | Register a doctor |

| GET | `/api/doctors` | Retrieve all doctors |



\### Register a doctor



Send a POST request to `http://localhost:8080/api/doctors` with this JSON body:



```json

{

&#x20; "name": "Dr. Ramesh",

&#x20; "specialization": "General Physician"

}

```



A successful request returns `201 Created`.



A request with an empty doctor name is rejected with `400 Bad Request`.



\## Tech Stack



\- Java 17

\- Spring Boot

\- Spring Security

\- Spring Data JPA

\- PostgreSQL

\- Redis

\- Maven

\- Docker

\- Postman



\## Future Plans



\- Patient token generation

\- Live queue position and estimated waiting time

\- WebSocket-based real-time updates

\- Frontend application



\## License



This project is currently a work in progress.

