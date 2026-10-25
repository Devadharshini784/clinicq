## Staff Authentication (JWT)

ClinicQ uses JSON Web Tokens (JWT) to authenticate staff members.

### Login Endpoint

**POST** `/api/auth/login`

Request body:

```json
{
  "username": "reception",
  "password": "reception123"
}
```

A successful login returns a JWT token, username, and role.

### Get Current Staff User

**GET** `/api/auth/me`

Requires a valid JWT in the `Authorization` header:

```text
Authorization: Bearer <your-jwt-token>
```

A successful request returns the authenticated username and authorities.

### Development Accounts

The application seeds the following development accounts when the staff user table is empty:

| Username | Password | Role |
|---|---|---|
| `reception` | `reception123` | RECEPTIONIST |
| `doctor` | `doctor123` | DOCTOR |

**Security note:** These are development-only credentials. Change or remove them before deploying to production.

### Authentication Testing

The following behaviors have been tested using Postman:

- Valid login returns `200 OK` and a JWT.
- Accessing `/api/auth/me` with a valid token returns `200 OK`.
- Accessing `/api/auth/me` without a token returns `401 Unauthorized`.
- Login with an incorrect password returns `401 Unauthorized`.