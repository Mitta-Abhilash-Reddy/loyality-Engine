# AI-Powered Loyalty Engagement Engine

A full-stack enterprise loyalty platform built with **Spring Boot**, **PostgreSQL**, and a **Python AI microservice** for churn prediction. Demonstrates real-world SaaS thinking aligned with loyalty platforms like GRAVTY®.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Dashboard                    │
│              (HTML/CSS/JS — loyalty-dashboard/)          │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP + JWT
┌───────────────────────▼─────────────────────────────────┐
│                Spring Boot API  :8080                    │
│                                                          │
│  Auth → Customer → Transaction → Offer → Notification   │
│                         │                               │
│              Analytics Dashboard API                     │
└───────────────────────┬─────────────────────────────────┘
          │             │
          │ async REST  │ JPA
┌─────────▼──────┐  ┌───▼──────────────┐
│  Python Flask  │  │   PostgreSQL      │
│  Churn Scorer  │  │   loyalty_engine  │
│  :8001         │  │   :5432           │
└────────────────┘  └───────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2.0, Maven |
| Database | PostgreSQL 15 |
| AI Service | Python 3.x, Flask |
| Auth | JWT (jjwt 0.11.5), Spring Security |
| Async | Spring `@Async` |
| Frontend | Vanilla HTML/CSS/JS (zero dependencies) |

---

## Features

- **Customer Management** — register, login, profile updates, soft delete
- **Points Engine** — earn (₹10 = 1 point), redeem, weekend double points, auto tier upgrades
- **Churn Prediction** — Python rule-based scorer called async after every transaction
- **Offer Engine** — auto-assigns personalized offers based on churn score + tier
- **Notifications** — async notification log with retry logic (up to 3 attempts)
- **Analytics API** — summary stats, at-risk customers, top customers, offer performance
- **Dashboard** — live frontend with churn risk bars, tier badges, offer/notification lookup

---

## Getting Started

### Prerequisites
- Java 17
- Maven
- PostgreSQL 15
- Python 3.x

### 1. Database Setup
```sql
CREATE DATABASE loyalty_engine;
```

### 2. Configure application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/loyalty_engine
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your_secret_key_here
jwt.expiration-ms=86400000
loyalty.points.rate=10
churn.service.url=http://localhost:8001/score
```

### 3. Start Python Churn Service
```bash
cd churn-service
pip install -r requirements.txt
python app.py
# Runs on http://localhost:8001
```

### 4. Start Spring Boot
```bash
mvn spring-boot:run
# Runs on http://localhost:8080
```

### 5. Open Dashboard
Open `loyalty-dashboard/index.html` in your browser.
Login with: `ravi@example.com` / `ravi1234`

---

## API Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new customer |
| POST | `/api/auth/login` | Login → JWT token |

### Customers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | Paginated list (JWT required) |
| GET | `/api/customers/{id}` | Single customer |
| PUT | `/api/customers/{id}` | Update name/phone |
| DELETE | `/api/customers/{id}` | Soft delete |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Earn points |
| POST | `/api/transactions/redeem` | Redeem points |
| GET | `/api/transactions/{customerId}` | History (paginated, date filter) |

### Offers
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/offers/assign/{customerId}` | Manually trigger offer |
| GET | `/api/offers/{customerId}` | All offers |
| GET | `/api/offers/{customerId}/active` | Active offers only |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications/{customerId}` | Notification history |
| POST | `/api/notifications/retry-failed` | Retry all failed |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/summary` | Platform summary |
| GET | `/api/analytics/at-risk` | Customers with churnScore ≥ 0.7 |
| GET | `/api/analytics/top-customers` | Top 10 by points |
| GET | `/api/analytics/offer-performance` | Offer stats + redemption rate |

### Churn Service (Python :8001)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Service health check |
| POST | `/score` | Score a customer → churnScore + riskLevel |

---

## Churn Scoring Rules

| Condition | Score Impact |
|-----------|-------------|
| Days since last transaction > 30 | +0.4 |
| Days since last transaction > 14 | +0.2 |
| Total points = 0 | +0.3 |
| Tier = BRONZE | +0.1 |
| Active in last 7 days | −0.2 |

Score is clamped to `[0.0, 1.0]`.
Risk levels: **LOW** < 0.4 · **MEDIUM** 0.4–0.69 · **HIGH** ≥ 0.7

---

## Offer Assignment Rules

| Tier | Offer (when churnScore ≥ 0.7) |
|------|-------------------------------|
| BRONZE | Double Points This Weekend |
| SILVER | 500 Bonus Points on Next Purchase |
| GOLD | Exclusive Lounge Access Offer |
| PLATINUM | Exclusive Lounge Access Offer |

Only one active offer per customer at a time. Auto-triggered after every churn score update.

---

## Tier Thresholds

| Tier | Points Required |
|------|----------------|
| BRONZE | 0 – 999 |
| SILVER | 1,000 – 4,999 |
| GOLD | 5,000 – 9,999 |
| PLATINUM | 10,000+ |

---

## Full Async Flow

```
POST /api/transactions
        │
        ▼
TransactionService (earn/redeem, tier upgrade)
        │
        ▼ @Async
ChurnScoringService (calls Python :8001, updates churn_score)
        │
        ▼ if score ≥ 0.7
OfferService (assigns offer if no active offer exists)
        │
        ▼ @Async
NotificationService (logs notification, retries up to 3x)
```

---

## Test Users

| Email | Password | Tier | Notes |
|-------|----------|------|-------|
| ravi@example.com | ravi1234 | SILVER | Primary test user, dashboard login |
| verify@example.com | (set on register) | BRONZE | Secondary test user |

---

## Project Structure

```
loyalty-engine/
├── src/main/java/com/loyalty/
│   ├── auth/           JWT auth
│   ├── config/         SecurityConfig, AppConfig, CorsConfig, GlobalExceptionHandler
│   ├── customer/       Customer entity + API
│   ├── transaction/    Transaction entity + points engine
│   ├── churn/          Async churn scoring client
│   ├── offer/          Offer engine + API
│   ├── notification/   Async notification service + API
│   ├── analytics/      Analytics dashboard API
│   └── seeder/         DataSeeder (20 customers + 100+ transactions)
├── churn-service/      Python Flask churn scorer
└── loyalty-dashboard/  Frontend (single HTML file)
```
