# **AccessGuard Spec Sheet (V1)**

---

## **Overview**

AccessGuard is a multi-tenant authentication and authorization platform designed to operate like a hosted auth provider (e.g., Clerk/Auth0) with an API-first architecture. It supports user login, role-based access control (RBAC), JWT issuance, public key token verification, event logging, and suspicious activity notifications.

---

## **Core Features**

1. **Tenant Management**
    - Tenants can register with AccessGuard
    - Each tenant receives isolated user/role namespace
    - Each tenant has an API key and a public/private key pair for JWT signing/verification
2. **User Management**
    - Register user (with metadata: email, phone, etc.)
    - Login user (issues JWT)
    - Refresh token support (future)
    - Logout user (optional token revocation, V2)
3. **Role Management**
    - Tenants define roles
    - Assign roles to users
    - Roles are embedded in JWT claims
4. **JWT Support**
    - Signed using RS256
    - Claims: `sub`, `tenant_id`, `roles`, `iat`, `exp`, `jti`
    - Exposed public keys via JWKS (`/.well-known/jwks.json`)
    - Token verification occurs on the tenant's server using the public key
5. **Usage Tracking via Webhook**
    - Aspect Oriented Annotations used to make calls to usage service
    - Logs actions in Elastic Search for further analysis if required
6. **Login Event Logging + Notification**
    - On successful login, AccessGuard publishes a Kafka event
    - A consumer service:
        - Logs the login event in `LoginHistory`
        - ~~Compares login IP to TrustedIP for that user~~
        - ~~If new/unseen IP, sends notification (email/SMS/webhook)~~
7. **~~Trusted IP Management~~**
    - ~~Maintains per-user `TrustedIP` list~~
    - ~~Can auto-enroll IPs post-login or keep manual-only~~

---

## **Endpoints**

### 🚩 **Tenant Endpoints**

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/v1/tenant/register` | Register new tenant (`Tenant`) |
| GET | `/v1/tenant/{id}` | Fetch tenant metadata |
| PATCH | `/v1/tenant/{id}` | Update tenant (e.g., name) |
| DELETE | `/v1/tenant/{id}` | Deactivate/delete tenant (soft delete recommended) |
| PATCH | `/v1/tenant/keys` | Rotate public/private key pair (JWT) |

### 👤 **User Endpoints**

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/v1/user/register` | Register user under tenant |
| GET | `/v1/user/{id}` | Fetch user details |
| PATCH | `/v1/user/{id}` | Update user metadata (`email`, `phone`) |
| DELETE | `/v1/user/{id}` | Deactivate user (soft delete) |
| POST | `/v1/user/login` | Login user, issue JWT |
| POST | `/v1/user/{id}/assign-role` | Assign one or more roles |
| DELETE | `/v1/user/{id}/unassign-role` | Remove one or more roles |

### 🛡️ **Role Endpoints**

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/v1/role/create` | Define a role |
| GET | `/v1/role/{id}` | Get role by ID |
| GET | `/v1/role/list?tenantId=X` | List roles for a tenant |
| PATCH | `/v1/role/{id}` | Update role metadata |
| DELETE | `/v1/role/{id}` | Delete a role |

### 🔑 **Key & JWT**

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/v1/keys/public` | Return current public keys (JWKS) |
| GET | `/.well-known/jwks.json` | JWKS-compliant discovery endpoint |


### 🌍 **~~Trusted IP Management~~ (AXED)**

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/v1/trusted-ip/{userId}` | List user's trusted IPs |
| POST | `/v1/trusted-ip` | Add IP manually |
| DELETE | `/v1/trusted-ip/{id}` | Remove a trusted IP |

---

### **Kafka Topics**

- `accessguard.login.events` (produced by AuthService)
- (Future) `accessguard.alerts.generated` (if alerting is decoupled)

---

### **Data Models**

- `Tenant`
    - `tenantID`, `name`, `apiKeyHash`, `publicKey`, `privateKey`, `createdAt`
- `User`
    - `tenantID`, `email`, `phone`, `roles`,`passwordHash` ,`createdAt`,`passwordUpdatedAt`
- `Role`
    - `id`, `tenantId`, `name`, `description`
- `~~TrustedIP~~`
    - `~~id`, `userId`, `ip`, `createdAt`~~
- `LoginHistory`
    - `id`, `userId`, `ip`, `ua`, `timestamp`, `tenantId`
- `UsageLog`
    - `id`, `tenantId`, `userId`, `tokenId`, `ip`, `ua`, `endpoint`, `method`, `timestamp`

---

## **Services & Responsibilities**

### **1. AuthService**

**Endpoints:**

- `POST /v1/user/register`
- `POST /v1/user/login`
- `POST /v1/user/{id}/assign-role`
- `DELETE /v1/user/{id}/unassign-role`

**Functions:**

- Registers users into `User`
- Authenticates credentials (via tenant config)
- Issues JWT (claims include: `sub`, `tenant_id`, `roles`, `iat`, `exp`, `jti`)
- Publishes to Kafka: `accessguard.login.events`
- Performs IP enrichment and default `TrustedIP` logic

---

### **2. TenantService**

**Endpoints:**

- `POST /v1/tenant/register`
- `GET /v1/tenant/{id}`
- `PATCH /v1/tenant/{id}`
- `DELETE /v1/tenant/{id}`
- `PATCH /v1/tenant/keys`

**Functions:**

- Creates and manages `Tenant`
- Assigns API keys and key pairs (JWT signing)
- Controls manual key rotation (`privateKey`, `publicKey`)
- Performs soft-deletion with cascading policy

---

### **3. RoleService**

**Endpoints:**

- `POST /v1/role/create`
- `GET /v1/role/{id}`
- `GET /v1/role/list?tenantId=X`
- `PATCH /v1/role/{id}`
- `DELETE /v1/role/{id}`

**Functions:**

- Manages `Role` data
- Allows per-tenant role creation
- Supports assignment metadata (used by AuthService)

---

### **4. NotificationService**

**Triggered via Kafka only**

**Reads:**

- Kafka Topic: `accessguard.login.events`

**Functions:**

- Checks `TrustedIP` per `User`
- Writes to `LoginHistory` (id, ip, ua, tenantId)
- Sends notification if IP is suspicious:
    - Email (SMTP or 3rd-party)
    - SMS (Twilio or similar)
    - Webhook (future)
- Failsafe: Retry logic or DLQ (V2)

---

### **5. UsageService**

**Endpoints:**

- `POST /v1/usage`
- `GET /v1/usage/{tenantId}`

**Functions:**

- Accepts structured payloads
- Writes to `UsageLog` (tokenId, ip, ua, endpoint, etc.)
- Authenticates via `apiKey` (from `Tenant`)
- Provides tenant-filtered analytics endpoint

---

### **~~6. TrustedIPService~~**

**~~Endpoints:~~**

- `~~GET /v1/trusted-ip/{userId}~~`
- `~~POST /v1/trusted-ip~~`
- `~~DELETE /v1/trusted-ip/{id}~~`

**~~Functions:~~**

- ~~Reads/writes to `TrustedIP`~~
- ~~Validates IPs and applies tenant-specific policy~~
- ~~Optionally supports auto-enrollment mode~~

---

### **7. KeyService**

**Endpoints:**

- `GET /v1/keys/public`
- `GET /.well-known/jwks.json`

**Functions:**

- Serves JWKS for each tenant’s public key
- Supports dynamic refresh from DB
- Backed by Redis or in-memory cache for perf

---

### **8. GatewayService**

**Internal-only; routes all external requests**

**Functions:**

- Validates JWT on incoming calls
- Verifies via `KeyService` + cached JWKS
- Adds tenant context for downstream services
- Performs rate limiting (Redis token bucket pattern)
- Logs structured request metadata (for auditing/future observability)

---

### **9. RegistryService (Eureka)**

**Internal service discovery**

**Functions:**

- Allows service-to-service resolution (Feign clients)
- Supports dynamic scaling
- Zero runtime config for downstream services

---

### **10. Kafka Broker**

**Transport for async events**

**Topics:**

- `accessguard.login.events`
- (future) `accessguard.alerts.generated`

**Functions:**

- Guarantees event propagation
- Allows decoupled pipelines (logging, ML, risk engine in V2+)

---

### **11. Database Layer**

| Component | Description |
| --- | --- |
| **PostgreSQL** | Stores all structured data: `Tenant`, `User`, `Role`, `TrustedIP`, `LoginHistory`, `UsageLog` |
| **Redis** | Session cache, rate limiting, possibly JWKS caching |
| ElasticSearch(optional) | Flexible store for extensible metadata fields or logs |

---

## **Technology Stack**

- Spring Boot
- Spring Security
- Spring Cloud
- Kafka
- PostgreSQL
- Redis (phase 2)
- ElasticSearch
- Docker (deployment)

---

## **Deferred Features (V2+)**

- Refresh token rotation
- Admin UI
- Token revocation lists
- Fine-grained permission system
- Social login (OAuth2)
- SDKs for client usage reporting
- WebAuthn / MFA
- Role hierarchy and permission expansion

---

## **Security Considerations**

- JWTs signed using RS256 to prevent tampering
- Tenant API keys are required for usage/event endpoints
- IP-based anomaly detection for login alerts
- All events logged for auditing purposes

---

**Assumptions**

- Tenants handle API request authentication using AccessGuard-issued JWTs
- Tenants are responsible for passing accurate usage data including IP/UA
- Public key rotation will be manual in V1

## Userflows

### **1. Tenant Onboarding Flow**

**Actor:** A new app/startup/dev signs up to use AccessGuard.

1. `POST /v1/tenant/register`
    
    → Registers the tenant, returns `apiKey`, `publicKey`, `privateKey`.
    
2. Tenant stores these keys securely (probably only the private one server-side).
3. From now on, all AccessGuard requests made by the tenant include the `apiKey` for usage reporting.

---

### **2. User Registration Flow**

**Actor:** End-user signing up on the tenant's platform (handled via backend call by tenant).

1. Tenant calls `POST /v1/user/register`
    
    → Payload: `email`, `phone`, etc., scoped to that tenant.
    
    → AccessGuard stores the user in the tenant’s namespace.
    
2. AccessGuard returns a user ID (tenant’s system may store it internally).

---

### **3. Login Flow (Token Issuance)**

**Actor:** End-user attempts to log in to the tenant's platform.

1. Tenant backend verifies credentials.
2. Tenant calls `POST /v1/user/login`
    
    → Payload: tenant ID, user ID or credentials
    
    → AccessGuard:
    
    - Validates the user
    - Issues RS256 JWT with claims
    - Publishes `accessguard.login.events` to Kafka
    - Returns JWT
3. Tenant uses this JWT to secure its own backend APIs.

---

### **4. Token Verification Flow**

**Actor:** Tenant’s backend verifying JWTs on protected endpoints.

1. When JWT comes in:
    - Tenant's backend reads `tenant_id` claim
    - Looks up public key for the tenant from:
        - `GET /.well-known/jwks.json`
    - Verifies RS256 signature
    - Decodes claims (`sub`, `roles`, etc.)
2. If valid, tenant proceeds with business logic.


---

### **5. Login Notification Flow (New IP Detection)**

**Actor:** Kafka consumer reacting to login events.

1. AuthService publishes `accessguard.login.events`.
2. NotificationService:
    - ~~Checks `TrustedIP` list~~
    - ~~If IP is new, stores in `LoginHistory`, optionally updates `TrustedIP`~~
    - Triggers:
        - Email
        - SMS
        - Webhook call (tenant-defined, future)

---

### **~~6. Trusted IP Enrollment Flow~~**

**~~Two modes:~~**

- **~~Auto mode** (default): IP added to `TrustedIP` after successful login.~~
- **~~Manual mode** (V2): Tenant exposes their own UI/API to manage IPs. AccessGuard just serves/validates.~~

---

### **7. Public Key Rotation Flow (Manual, V1)**

**Actor:** Tenant rotates keys and notifies AccessGuard.

1. Tenant generates a new key pair.
2. Calls `PATCH /v1/tenant/keys` (future V1.1 endpoint)
3. AccessGuard stores the new pair.
4. Tenant starts signing JWTs with the new private key.
5. Verification by clients still works as JWKS serves updated public key.

---

### **8. Failure / Anomaly Handling Flow**

**Examples:**

- Tenant sends invalid usage data → 400 Bad Request
- JWT expired → Tenant handles 401
- NotificationService fails to notify → Kafka retry or dead-letter (V2)

---

### **9. Service-to-Service Flow (Internal)**

**Actor:** AccessGuard services talking to each other (Eureka + Feign)

- AuthService → Kafka → NotificationService
- UsageService persists data to DB
- KeyService serves public keys
- GatewayService handles routing to appropriate downstream service
