# NeuroGuard Microservices - Complete Setup Guide

## Overview

NeuroGuard is a complete microservices-based Alzheimer's patient monitoring platform with:
- **Keycloak OAuth2** security at the API Gateway
- **Feign Client** for synchronous inter-service communication
- **RabbitMQ** for asynchronous event-driven communication
- **PostgreSQL** for medical-history-service (advanced database technology)
- **Docker & Docker Compose** for containerized deployment
- **Service Discovery** via Eureka
- **API Gateway** for centralized routing

---

## Architecture Overview

### Services:
1. **Gateway** (Port 8083) - API Gateway with OAuth2/Keycloak
2. **Eureka Server** (Port 8761) - Service Registry
3. **Care Plan Service** (Port 8082) - MySQL, Feign clients, RabbitMQ
4. **Consultation Service** (Port 8084) - MySQL, Feign clients, RabbitMQ
5. **Medical History Service** (Port 8086) - **PostgreSQL**, Feign clients, RabbitMQ
6. **User Service** (Port 8081) - Node.js, MongoDB
7. **Keycloak** (Port 8080) - OAuth2/OpenID Connect Authentication

### Databases:
- **PostgreSQL** (Port 5432) - medical-history-service
- **MySQL** (Port 3306) - careplan-service, consultation-service
- **MongoDB** (Port 27017) - user-service
- **RabbitMQ** (Port 5672, Admin: 15672) - Event messaging

---

## Prerequisites

### For Local Development:
- Java 17+ (JDK)
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose (for containerized setup)
- Keycloak running on port 8080

### For Docker Deployment:
- Docker 20.10+
- Docker Compose 2.0+

---

## Setup Instructions

### Option 1: Local Development (Without Docker)

#### 1. Start Databases and Message Broker

```bash
# PostgreSQL
docker run -d --name neuroguard-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=medical_history_db \
  -p 5432:5432 \
  postgres:15-alpine

# MySQL
docker run -d --name neuroguard-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=careplandb \
  -p 3306:3306 \
  mysql:8.0

# MongoDB
docker run -d --name neuroguard-mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=root \
  -p 27017:27017 \
  mongo:6.0

# RabbitMQ
docker run -d --name neuroguard-rabbitmq \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.12-management-alpine

# Keycloak
docker run -d --name neuroguard-keycloak \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -p 8080:8080 \
  quay.io/keycloak/keycloak:23.0.0 \
  start-dev
```

#### 2. Build All Microservices

```bash
cd BackEnd

# Build all services
mvn clean package -DskipTests

# Or build individual services
cd eureka-server && mvn clean package -DskipTests && cd ..
cd gateway && mvn clean package -DskipTests && cd ..
cd careplan-service && mvn clean package -DskipTests && cd ..
cd consultation-service && mvn clean package -DskipTests && cd ..
cd medical-history-service && mvn clean package -DskipTests && cd ..
```

#### 3. Start Services in Order

```bash
# Start Eureka Server (in new terminal)
cd BackEnd/eureka-server
java -jar target/eureka-server-0.0.1-SNAPSHOT.jar

# Start Gateway (in new terminal)
cd BackEnd/gateway
java -jar target/gateway-0.0.1-SNAPSHOT.jar

# Start Care Plan Service (in new terminal)
cd BackEnd/careplan-service
java -jar target/careplan-service-0.0.1-SNAPSHOT.jar

# Start Consultation Service (in new terminal)
cd BackEnd/consultation-service
java -jar target/consultation-service-0.0.1-SNAPSHOT.jar

# Start Medical History Service (in new terminal)
cd BackEnd/medical-history-service
java -jar target/medical-history-service-0.0.1-SNAPSHOT.jar

# Start User Service (in new terminal)
cd BackEnd/user-service
npm install && npm start
```

#### 4. Verify Services

- Eureka Dashboard: http://localhost:8761
- Gateway: http://localhost:8083
- Keycloak: http://localhost:8080/admin
- RabbitMQ Management: http://localhost:15672 (guest/guest)

---

### Option 2: Docker Compose (Recommended)

#### 1. Build Docker Images

```bash
cd BackEnd

# Build all service images
mvn clean package -DskipTests

# Build Docker images
docker build -t neuroguard/gateway:latest ./gateway
docker build -t neuroguard/eureka-server:latest ./eureka-server
docker build -t neuroguard/careplan-service:latest ./careplan-service
docker build -t neuroguard/consultation-service:latest ./consultation-service
docker build -t neuroguard/medical-history-service:latest ./medical-history-service
docker build -t neuroguard/user-service:latest ./user-service
```

#### 2. Start Entire Stack

```bash
cd BackEnd

# Start all services with one command
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

#### 3. Verify Docker Services

```bash
# Check running containers
docker-compose ps

# Check logs for specific service
docker-compose logs gateway
docker-compose logs careplan-service

# Access services
# Eureka: http://localhost:8761
# Gateway: http://localhost:8083
# Keycloak: http://localhost:8080
# RabbitMQ: http://localhost:15672 (guest/guest)
```

---

## Keycloak Configuration

### 1. Access Keycloak Admin Console
- URL: http://localhost:8080/admin
- Username: `admin`
- Password: `admin`

### 2. Create Realm

1. Click **"Manage Realms"** → **"Create Realm"**
2. Realm name: `neuroguard`
3. Click **"Create"**

### 3. Create Gateway Client

1. Go to **Clients** → **"Create client"**
2. Client ID: `gateway`
3. Client authentication: **OFF**
4. Redirect URIs: `http://localhost:4200/*`
5. Save and note the credentials

### 4. Create Service Client

1. Go to **Clients** → **"Create client"**
2. Client ID: `neuroguard-service`
3. Client authentication: **ON**
4. Client Authenticator: **Client id and secret**
5. Save and note the Client Secret

### 5. Create Roles

Go to **Realm roles** → **Create role**:
- `ROLE_PATIENT`
- `ROLE_PROVIDER`
- `ROLE_CAREGIVER`
- `ROLE_ADMIN`

### 6. Create Test Users

1. Go to **Users** → **"Create user"**
2. Create users for each role
3. Set passwords (not temporary)
4. Assign roles in **"Role mappings"**

### 7. Get Token Endpoint

1. Go to **Realm Settings** → **Endpoints**
2. Copy **OpenID Endpoint Configuration** URL
3. Extract `token_endpoint` from the JSON

---

## Testing Microservice Communication

### Test Feign Synchronous Communication

```bash
# Get a Keycloak token
TOKEN=$(curl -X POST http://localhost:8080/realms/neuroguard/protocol/openid-connect/token \
  -d "client_id=gateway" \
  -d "username=testuser" \
  -d "password=password" \
  -d "grant_type=password" | jq -r '.access_token')

# Call Care Plan Service through Gateway
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8083/api/careplan/1

# Call Consultation Service through Gateway
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8083/api/consultation/1
```

### Test RabbitMQ Asynchronous Communication

1. Access RabbitMQ Management: http://localhost:15672
2. Check queues created:
   - `careplan.created`
   - `careplan.updated`
   - `careplan.deleted`
   - `consultation.created`
   - `consultation.completed`
   - `medical-history.created`
   - `medical-history.updated`

3. Create/Update resources and verify messages in queues

---

## Key Features Implemented

### 1. Keycloak OAuth2 Security ✅
- Gateway validates Keycloak JWT tokens
- Token relay to downstream microservices
- Role-based access control (RBAC)
- Centralized authentication

### 2. Feign Client Communication ✅
**Scenario 1**: Careplan ← → Medical History
- `careplan-service` calls `medical-history-service` to fetch patient medical records

**Scenario 2**: Consultation ← → Medical History
- `consultation-service` calls `medical-history-service` to verify patient data

**Scenario 3**: Consultation ← → Careplan
- `consultation-service` calls `careplan-service` to check patient care plans

**Scenario 4**: Medical History ← → Careplan
- `medical-history-service` calls `careplan-service` to link medical records

### 3. RabbitMQ Event-Driven Communication ✅
**Publisher Events**:
1. Care Plan Created/Updated/Deleted
2. Consultation Created/Completed
3. Medical History Created/Updated

**Consumer Events**:
1. Care Plan Service listens to Consultation Completed
2. Consultation Service listens to Care Plan Created
3. Medical History Service listens to Care Plan Updated

### 4. Advanced Database Technology ✅
- **medical-history-service** migrated from H2 to **PostgreSQL**
- Better performance, scalability, and data persistence

### 5. Docker & Docker Compose ✅
- All services containerized
- Single `docker-compose up` deploys entire stack
- Data persistence with volumes
- Health checks configured

---

## Configuration Summary

### Port Assignments
| Service | Port | Database | Auth |
|---------|------|----------|------|
| Gateway | 8083 | - | Keycloak |
| Eureka | 8761 | - | - |
| Care Plan | 8082 | MySQL | OAuth2 |
| Consultation | 8084 | MySQL | OAuth2 |
| Medical History | 8086 | PostgreSQL | OAuth2 |
| User Service | 8081 | MongoDB | JWT |
| Keycloak | 8080 | H2 | - |

### RabbitMQ Exchanges
- `careplan-exchange`: Care plan events
- `consultation-exchange`: Consultation events
- `medical-history-exchange`: Medical history events

### Environment Variables (Docker)
```yaml
DATABASE_URL: PostgreSQL/MySQL connection URLs
RABBITMQ_HOST: rabbitmq
RABBITMQ_PORT: 5672
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/neuroguard
```

---

## Troubleshooting

### Services won't start
- Check if ports are already in use: `lsof -i :8083`
- Verify databases are running
- Check logs: `docker-compose logs service-name`

### Keycloak not accessible
- Ensure Keycloak is running: `docker ps | grep keycloak`
- Wait for startup: Keycloak takes 30-60 seconds to start
- Check health: http://localhost:8080/health

### RabbitMQ issues
- Check connection in logs: `docker-compose logs rabbitmq`
- Access management UI: http://localhost:15672

### Database connection errors
- Verify database containers are healthy: `docker-compose ps`
- Check credentials in application.yaml
- For PostgreSQL: `docker-compose exec postgres psql -U postgres -l`

---

## Next Steps

1. Configure Keycloak with realm, clients, and users
2. Test token generation and microservice calls
3. Monitor RabbitMQ message flow
4. Deploy to production with proper secrets management
5. Implement distributed tracing (Zipkin/Jaeger)
6. Add circuit breaker for Feign calls (Resilience4j)

---

## Support

For issues or questions:
1. Check application logs
2. Verify all containers are running
3. Ensure databases are accessible
4. Check Keycloak configuration
5. Verify RabbitMQ queues

**Total Implementation Time**: ~4-6 hours for full setup and testing
