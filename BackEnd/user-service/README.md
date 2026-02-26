# 🧠 NeuroGuard — User Management Microservice

User authentication microservice for the **NeuroGuard** Alzheimer's patient monitoring platform.

## Quick Start

```bash
# 1. Install dependencies
npm install

# 2. Configure environment (edit .env with your MongoDB URI)
cp .env.example .env

# 3. Start in development mode
npm run dev
```

> **Requires MongoDB** — either locally (`mongod`) or a free [MongoDB Atlas](https://www.mongodb.com/atlas) cluster.

---

## API Endpoints

### `GET /api/health` — Health Check

```bash
curl http://localhost:3001/api/health
```

### `POST /api/auth/register` — Create Account

```bash
curl -X POST http://localhost:3001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sarah",
    "lastName": "Connor",
    "email": "sarah@example.com",
    "password": "secure123",
    "role": "doctor"
  }'
```

**Roles**: `patient` | `caregiver` | `doctor` | `admin`

### `POST /api/auth/login` — Authenticate

```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sarah@example.com",
    "password": "secure123"
  }'
```

---

## Project Structure

```
user-service/
├── src/
│   ├── config/db.js            # MongoDB connection
│   ├── models/User.js          # User schema & password hashing
│   ├── controllers/auth.controller.js  # Register & login logic
│   ├── routes/auth.routes.js   # Route definitions
│   ├── middleware/validate.js  # Input validation rules
│   └── utils/token.js          # JWT helpers
├── server.js                   # Entry point
├── .env.example                # Env template
└── package.json
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `3001` | Server port |
| `MONGO_URI` | `mongodb://localhost:27017/neuroguard-users` | MongoDB connection string |
| `JWT_SECRET` | — | Secret key for signing JWTs |
| `JWT_EXPIRES_IN` | `7d` | Token expiration duration |
