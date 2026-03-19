## Voldemort 
# Project Overview
A microservices-based stock trading/protfolio application with :
  -**Angular frontend** with interactive charts via `chart.js` and `ng2-charts`
- **Multiple Spring Boot microservices** handling JWT auth, company CRUD, market shares, orders, portfolio, wallet, and time-series prices
- **PostgreSQL** as the shared database (each service uses JPA `ddl-auto=update`)
  
## 🛠️ Tech Stack
 
| Layer       | Technology                                      |
|-------------|--------------------------------------------------|
| Frontend    | Angular (CLI 21.2.0), chart.js, ng2-charts       |
| Backend     | Spring Boot, Java 17, Maven                      |
| Database    | PostgreSQL                                       |
| Auth        | Spring Security + JWT                            |
| Email       | SMTP (Gmail) + Thymeleaf templates               |
 
## 🌐 Services & Default Ports
 
| Service                        | Folder                                  | URL                          |
|--------------------------------|-----------------------------------------|------------------------------|
| Auth / Demo service            | `backend/demo`                          | http://localhost:5000         |
| Company service                | `backend/company`                       | http://localhost:5001         |
| Company time-series prices     | `backend/company-time-series-shares`    | http://localhost:5002         |
| Wallet service                 | `backend/wallet`                        | http://localhost:5003         |
| Portfolio service              | `backend/portfolio-service`             | http://localhost:5007         |
| Order service                  | `backend/order`                         | http://localhost:5008         |
| Market service                 | `backend/market`                        | http://localhost:5009         |
| Frontend                       | `frontend/`                             | http://localhost:4200         |
 
---
 
## ✅ Prerequisites
 
- **Node.js** + **npm**
- **Angular CLI** *(optional — can run via npm scripts)*
- **Java 17**
- **Maven**
- **PostgreSQL** running locally
 
---




## 🗂️ Project Structure

```
voldemort/
├── backend/
│   ├── demo/                        # Auth service
│   ├── company/                     # Company CRUD service
│   ├── company-time-series-shares/  # Time-series price service
│   ├── wallet/                      # Wallet service
│   ├── portfolio-service/           # Portfolio service
│   ├── order/                       # Order service
│   └── market/                      # Market service
└── frontend/                        # Angular web client
```


## 🗄️ Database Setup

1. Create a PostgreSQL database:

```sql
CREATE DATABASE Wissen_Final_Project;
```

2. Update each backend service's `application.properties` to match your local DB credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/Wissen_Final_Project
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
```


## ▶️ Running the Backend

Start each service individually from its folder. For example:

```bash
cd backend/demo
mvn spring-boot:run
```

Repeat for each service under `backend/`:

```bash
cd backend/company && mvn spring-boot:run
cd backend/company-time-series-shares && mvn spring-boot:run
cd backend/wallet && mvn spring-boot:run
cd backend/portfolio-service && mvn spring-boot:run
cd backend/order && mvn spring-boot:run
cd backend/market && mvn spring-boot:run
```

---

## 🎨 Running the Frontend

```bash
cd frontend
npm install
npm start
```

Open your browser at **http://localhost:4200**.

---

## 📧 Email (SMTP) Configuration

Email settings are configured in `backend/demo/src/main/resources/application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD

# Used for password reset links
app.base-url=http://localhost:4200
```

### Email Templates

Located in `backend/demo/src/main/resources/templates/`:

| Template                  | Purpose                  |
|---------------------------|--------------------------|
| `welcome-email.html`      | Sent on user registration |
| `reset-password.html`     | Password reset link email |
| `otp-email.html`          | OTP verification email    |

---


