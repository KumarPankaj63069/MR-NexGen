# MR NexGen IT Services — Backend API & Deployment Guide

Official backend REST API service for **MR NexGen IT Services** (`https://www.mrnexgen.com`).

---

## 1. Quick Start (Local & Production)

### Requirements
- Node.js 18+ or Docker
- SQLite 3 (built-in) or PostgreSQL

### Installation
```bash
cd backend
npm install
cp .env.example .env
npm start
```
Default server starts at `http://localhost:8080`.

---

## 2. Environment Configuration (`.env`)

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Listening server port | `8080` |
| `NODE_ENV` | Runtime environment (`development`/`production`) | `production` |
| `JWT_SECRET` | Secret token signing key | `mrnexgen_secure_production_secret_2026` |
| `DATABASE_URL` | Optional PostgreSQL / SQLite database URI | `sqlite://mrnexgen.db` |
| `SMTP_HOST` | Production SMTP Mail Server | `smtp.mrnexgen.com` |
| `SMTP_USER` | Support Email Address | `support@mrnexgen.com` |
| `SMTP_PASSWORD` | Secure Mail Password | *(Set in Secrets)* |

---

## 3. REST API Endpoint Specification

### Public Endpoints
- `GET /api/health` — System status
- `POST /api/auth/register` — Register user, returns `demoOtp` in dev
- `POST /api/auth/verify-email` — Verify 6-digit OTP
- `POST /api/auth/login` — Login, returns JWT bearer token
- `GET /api/services` — List published IT services
- `GET /api/portfolio` — List published portfolio projects
- `GET /api/blogs` — List published tech blogs
- `POST /api/contact` — Submit general client inquiry

### Authenticated Client Endpoints (`Authorization: Bearer <token>`)
- `POST /api/service-requests` — Submit project service request
- `GET /api/service-requests` — View user's submitted requests

### Admin Endpoints (`Authorization: Bearer <token>`, role: `ADMIN`)
- `GET /api/admin/requests` — View all system service requests
- `PUT /api/admin/requests/:id/status` — Update status (`APPROVED`, `IN_PROGRESS`, `COMPLETED`, `REJECTED`)
