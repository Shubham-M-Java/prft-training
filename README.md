# 🛒 Shopping Orchestrator AI

> A full-stack AI-powered shopping assistant built with **Spring Boot Microservices** + **Spring Cloud** (backend) and **Angular 17** (frontend). It uses 4 specialized AI agents that work together to research, compare, analyze budgets, and recommend the best products for Indian e-commerce (Amazon & Flipkart).

---

## 📋 Table of Contents

1. [Project Overview](#-project-overview)
2. [Architecture Diagram](#-architecture-diagram)
3. [Tech Stack](#-tech-stack)
4. [Project Structure](#-project-structure)
5. [Microservices Details](#-microservices-details)
6. [Data Models](#-data-models)
7. [Prerequisites](#-prerequisites)
8. [Step-by-Step Setup & Installation](#-step-by-step-setup--installation)
9. [Starting the Application](#-starting-the-application)
10. [All URLs & Endpoints](#-all-urls--endpoints)
11. [API Reference](#-api-reference)
12. [Database](#-database)
13. [Frontend UI Guide](#-frontend-ui-guide)
14. [Postman Collection](#-postman-collection)
15. [How It Works — End to End Flow](#-how-it-works--end-to-end-flow)
16. [Configuration Reference](#-configuration-reference)
17. [Troubleshooting](#-troubleshooting)
18. [Features Summary](#-features-summary)

---

## 🎯 Project Overview

Shopping Orchestrator AI is a **microservices-based** application where:

- A user enters a **product type**, **budget (₹)**, and **preferences** in the Angular UI
- The request flows through an **API Gateway** to an **Orchestrator Service**
- The Orchestrator coordinates **4 specialized AI agents** in sequence
- Each agent performs a specific task (research → compare → budget → recommend)
- The final result is a comprehensive shopping recommendation with product details, comparison table, budget analysis, and a final AI recommendation
- All searches are **saved to an H2 in-memory database** for history

---

## 🏗️ Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                    Angular UI  (port 4200)                        │
│         Dashboard · Search Form · Results · History               │
└──────────────────────────┬───────────────────────────────────────┘
                           │  HTTP (via proxy → port 8090)
┌──────────────────────────▼───────────────────────────────────────┐
│                  API Gateway  (port 8090)                         │
│         Routes: /api/** → orchestrator-service                    │
└──────────────────────────┬───────────────────────────────────────┘
                           │  HTTP
┌──────────────────────────▼───────────────────────────────────────┐
│             Orchestrator Service  (port 8081)                     │
│   • Coordinates all agents via OpenFeign                          │
│   • Saves search history to H2 database                           │
│   • Returns combined OrchestratorResponse                         │
└──────┬──────────┬──────────┬──────────────┬────────────────────┘
       │          │          │              │
  ┌────▼────┐ ┌───▼────┐ ┌───▼────┐ ┌──────▼──────┐
  │Research │ │Compare │ │Budget  │ │Recommend    │
  │Agent    │ │Agent   │ │Agent   │ │Agent        │
  │:8082    │ │:8083   │ │:8084   │ │:8085        │
  └────┬────┘ └───┬────┘ └───┬────┘ └──────┬──────┘
       │          │          │              │
┌──────▼──────────▼──────────▼──────────────▼──────────────────────┐
│               Eureka Service Registry  (port 8761)                │
│         All services register here for discovery                  │
└──────────────────────────────────────────────────────────────────┘
```

### Request Flow Summary
```
User Input → Angular (4200)
           → API Gateway (8090)
           → Orchestrator (8081)
               ├─ Step 1 → Product Research Agent (8082)  → returns product list
               ├─ Step 2 → Comparison Agent (8083)        → returns feature comparison
               ├─ Step 3 → Budget Agent (8084)            → returns budget analysis
               └─ Step 4 → Recommendation Agent (8085)   → returns best product
           → Saves to H2 DB
           → Returns OrchestratorResponse to UI
```

---

## 🔧 Tech Stack

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Programming language |
| Spring Boot | 3.2.x | Microservice framework |
| Spring Cloud Netflix Eureka | 2023.x | Service discovery & registry |
| Spring Cloud Gateway | 2023.x | API Gateway / reverse proxy |
| Spring Data JPA | 3.2.x | ORM for database access |
| H2 Database | 2.x | In-memory database (no setup needed) |
| OpenFeign | 2023.x | Declarative HTTP client for inter-service calls |
| Lombok | 1.18.x | Reduces boilerplate (getters/setters/builders) |
| Maven | 3.8+ | Build tool & dependency management |

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Angular | 17 | SPA framework |
| TypeScript | 5.x | Typed JavaScript |
| RxJS | 7.x | Reactive programming / HTTP observables |
| Angular Forms | 17 | Two-way data binding (ngModel) |
| CSS3 | - | Custom styles, animations, glassmorphism |
| Google Fonts (Inter + Poppins) | - | Typography |
| Font Awesome | 6.4 | Icons |

---

## 📁 Project Structure

```
shopping-orchestrator/
│
├── 📄 README.md                          ← This file
├── 📄 start-all.bat                      ← Windows: starts all 7 services at once
├── 📄 start-backend.bat                  ← Windows: starts only backend services
│
├── 📁 backend/
│   ├── 📄 pom.xml                        ← Parent Maven POM (manages all modules)
│   │
│   ├── 📁 eureka-server/                 ← Service Registry (port 8761)
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../EurekaServerApplication.java
│   │       └── resources/application.yml
│   │
│   ├── 📁 api-gateway/                   ← API Gateway (port 8090)
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../ApiGatewayApplication.java
│   │       └── resources/application.yml
│   │
│   ├── 📁 orchestrator-service/          ← Main Orchestrator (port 8081)
│   │   ├── pom.xml
│   │   └── src/main/java/com/shopping/orchestrator/
│   │       ├── OrchestratorApplication.java
│   │       ├── controller/OrchestratorController.java
│   │       ├── service/OrchestratorService.java
│   │       ├── client/                   ← Feign clients for each agent
│   │       ├── dto/                      ← Data Transfer Objects
│   │       │   ├── ShoppingRequest.java
│   │       │   ├── OrchestratorResponse.java
│   │       │   ├── ProductInfo.java
│   │       │   ├── ComparisonResult.java
│   │       │   ├── BudgetAnalysis.java
│   │       │   └── RecommendationResult.java
│   │       ├── entity/SearchHistory.java ← JPA entity for H2
│   │       └── repository/SearchHistoryRepository.java
│   │
│   ├── 📁 product-research-agent/        ← Research Agent (port 8082)
│   │   ├── pom.xml
│   │   └── src/main/java/com/shopping/research/
│   │       ├── ProductResearchApplication.java
│   │       ├── controller/ResearchController.java
│   │       ├── service/ProductResearchService.java
│   │       └── dto/ProductInfo.java
│   │
│   ├── 📁 comparison-agent/              ← Comparison Agent (port 8083)
│   │   ├── pom.xml
│   │   └── src/main/java/com/shopping/comparison/
│   │       ├── ComparisonApplication.java
│   │       ├── controller/ComparisonController.java
│   │       ├── service/ComparisonService.java
│   │       └── dto/
│   │
│   ├── 📁 budget-agent/                  ← Budget Agent (port 8084)
│   │   ├── pom.xml
│   │   └── src/main/java/com/shopping/budget/
│   │       ├── BudgetApplication.java
│   │       ├── controller/BudgetController.java
│   │       ├── service/BudgetService.java
│   │       └── dto/
│   │
│   └── 📁 recommendation-agent/          ← Recommendation Agent (port 8085)
│       ├── pom.xml
│       └── src/main/java/com/shopping/recommendation/
│           ├── RecommendationApplication.java
│           ├── controller/RecommendationController.java
│           ├── service/RecommendationService.java
│           └── dto/
│
├── 📁 frontend/                          ← Angular 17 App
│   ├── 📄 package.json                   ← Node dependencies
│   ├── 📄 angular.json                   ← Angular CLI config
│   ├── 📄 proxy.conf.json                ← Dev proxy: /api → localhost:8090
│   ├── 📄 tsconfig.json                  ← TypeScript config
│   └── src/
│       ├── index.html                    ← Root HTML (loads fonts, FA icons)
│       ├── main.ts                       ← Angular bootstrap
│       ├── styles.css                    ← Global styles & animations
│       └── app/
│           ├── app.component.ts          ← Root component
│           ├── components/
│           │   └── dashboard/
│           │       ├── dashboard.component.html  ← Main UI template
│           │       └── dashboard.component.ts    ← Component logic
│           ├── models/
│           │   └── shopping.models.ts    ← TypeScript interfaces
│           └── services/
│               └── shopping.service.ts  ← HTTP calls to backend
│
└── 📁 postman/
    └── Shopping-Orchestrator-API.postman_collection.json
```

---

## 🔬 Microservices Details

### 1. 🗂️ Eureka Server — Port `8761`
- **Role:** Service Registry & Discovery
- **What it does:** All other microservices register themselves here on startup. Services discover each other by name (not hardcoded IP/port).
- **Dashboard:** `http://localhost:8761` — shows all registered services
- **Key config:**
  ```yaml
  server:
    port: 8761
  spring:
    application:
      name: eureka-server
  eureka:
    client:
      register-with-eureka: false   # doesn't register itself
      fetch-registry: false
  ```
- **Must start FIRST** before any other service

---

### 2. 🚪 API Gateway — Port `8090`
- **Role:** Single entry point for the Angular frontend
- **What it does:** Receives all requests from the UI and routes them to the correct microservice. Handles CORS.
- **Routes:** `/api/**` → `orchestrator-service`
- **Key config:**
  ```yaml
  server:
    port: 8090
  spring:
    application:
      name: api-gateway
    cloud:
      gateway:
        routes:
          - id: orchestrator
            uri: lb://orchestrator-service
            predicates:
              - Path=/api/**
  ```
- **`lb://`** means load-balanced via Eureka

---

### 3. 🎯 Orchestrator Service — Port `8081`
- **Role:** Brain / Coordinator of all agents
- **What it does:**
  - Receives search request from API Gateway
  - Calls all 4 agents in sequence using **OpenFeign**
  - Combines all results into one `OrchestratorResponse`
  - Saves search to **H2 in-memory database**
  - Returns complete response to frontend
- **Database:** H2 in-memory (`jdbc:h2:mem:orchestratordb`)
- **H2 Console:** `http://localhost:8081/h2-console`
- **Feign timeouts:** connect=10s, read=30s
- **Key config:**
  ```yaml
  server:
    port: 8081
  agents:
    research:
      url: http://localhost:8082
    comparison:
      url: http://localhost:8083
    budget:
      url: http://localhost:8084
    recommendation:
      url: http://localhost:8085
  ```

---

### 4. 🔍 Product Research Agent — Port `8082`
- **Role:** Finds products matching the user's query
- **What it does:**
  - Accepts `productCategory`, `budget`, `preferences`
  - Searches for matching products (Amazon/Flipkart/Mock data)
  - Returns a list of `ProductInfo` objects with name, brand, price, rating, features, image URL, source URL
- **Endpoint:** `GET /api/research/products?productType=X&category=Y&budget=Z`

---

### 5. ⚖️ Comparison Agent — Port `8083`
- **Role:** Compares products feature-by-feature
- **What it does:**
  - Accepts a list of `ProductInfo` objects
  - Generates a feature comparison matrix
  - Produces pros & cons for each product
  - Returns a `ComparisonResult` with summary
- **Endpoint:** `POST /api/comparison/compare`

---

### 6. 💰 Budget Agent — Port `8084`
- **Role:** Budget analysis and value optimization
- **What it does:**
  - Filters products within the user's budget
  - Identifies the best value product
  - Suggests cheaper alternatives
  - Provides cost-vs-value insight text
- **Endpoint:** `POST /api/budget/analyze?budget=30000`

---

### 7. 🏆 Recommendation Agent — Port `8085`
- **Role:** Final AI-powered recommendation
- **What it does:**
  - Analyzes all data from previous agents
  - Picks the single best product
  - Provides reasoning for the recommendation
  - Lists alternative options
  - Generates a final decision statement
- **Endpoint:** `POST /api/recommendation/recommend?budget=30000&preferences=camera`

---

## 📐 Data Models

### ShoppingRequest (Input)
```json
{
  "productCategory": "Smartphone",
  "budget": 30000,
  "preferences": "camera quality, battery life",
  "brand": "Samsung",
  "additionalRequirements": "5G support"
}
```

### ProductInfo
```json
{
  "id": "prod-001",
  "name": "Samsung Galaxy S23",
  "brand": "Samsung",
  "category": "Smartphone",
  "price": 29999,
  "priceRange": "₹28,000 - ₹32,000",
  "keyFeatures": ["200MP Camera", "5000mAh Battery", "5G"],
  "notableHighlights": "Best camera in segment",
  "rating": 4.5,
  "imageUrl": "https://...",
  "sourceUrl": "https://amazon.in/...",
  "platform": "Amazon",
  "reviewCount": 12500,
  "availability": "In Stock",
  "discount": "15% off"
}
```

### OrchestratorResponse (Full Output)
```json
{
  "requestId": "uuid-xxxx",
  "timestamp": "2026-06-01T10:00:00",
  "productCategory": "Smartphone",
  "budget": 30000,
  "preferences": "camera quality",
  "topOptionsFound": [ ...ProductInfo[] ],
  "comparisonSummary": {
    "products": [...],
    "featureComparison": {
      "Camera": ["200MP", "108MP", "64MP"],
      "Battery": ["5000mAh", "4500mAh", "4000mAh"]
    },
    "prosAndCons": {
      "Samsung Galaxy S23": ["Excellent camera", "Fast charging"]
    },
    "summary": "Samsung leads in camera, OnePlus in performance"
  },
  "budgetAnalysis": {
    "userBudget": 30000,
    "productsWithinBudget": [...],
    "cheaperAlternatives": [...],
    "costVsValueInsight": "Best value at ₹28,999",
    "bestValueProduct": { ...ProductInfo }
  },
  "finalRecommendation": {
    "bestProduct": { ...ProductInfo },
    "reasonForRecommendation": "Best camera + battery combo under budget",
    "alternativeOptions": [...],
    "finalDecision": "Go for Samsung Galaxy S23 — it ticks all your boxes"
  },
  "status": "SUCCESS",
  "errorMessage": null
}
```

### SearchHistory (Database Entity)
```json
{
  "id": 1,
  "requestId": "uuid-xxxx",
  "productCategory": "Smartphone",
  "budget": 30000,
  "preferences": "camera quality",
  "bestProductName": "Samsung Galaxy S23",
  "bestProductPrice": 29999,
  "createdAt": "2026-06-01T10:00:00",
  "status": "SUCCESS"
}
```

---

## ✅ Prerequisites

Before running this project, ensure you have the following installed:

### 1. Java 17
```bash
# Verify installation
java -version
# Expected: openjdk version "17.x.x"
```
> 💡 This project uses Java from Eclipse JRE at:
> `C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre`

### 2. Apache Maven 3.8+
```bash
# Verify installation
mvn -version
# Expected: Apache Maven 3.x.x
```
> 💡 Maven is located at: `C:\maven\apache-maven-3.9.6\bin\mvn.cmd`

### 3. Node.js 18+ and npm 9+
```bash
# Verify installation
node -v    # Expected: v18.x.x or higher
npm -v     # Expected: 9.x.x or higher
```

### 4. Angular CLI (optional, for development)
```bash
npm install -g @angular/cli
ng version  # Expected: Angular CLI: 17.x.x
```

### 5. Git (optional)
```bash
git --version
```

---

## 🚀 Step-by-Step Setup & Installation

### Step 1 — Clone / Navigate to Project
```bash
cd c:\prft-training\shopping-orchestrator
```

### Step 2 — Install Frontend Dependencies
```bash
cd frontend
npm install
```
This installs all Angular packages listed in `package.json` into `node_modules/`.

> ⏱️ Takes 1–3 minutes on first run.

### Step 3 — Build Backend (Optional — Maven auto-downloads dependencies)
```bash
cd backend
mvn clean install -DskipTests
```
This compiles all 7 Spring Boot services and downloads Maven dependencies.

> ⏱️ Takes 3–5 minutes on first run (downloads from Maven Central).

---

## ▶️ Starting the Application

### ✅ Option A — One-Click Start (Recommended for Windows)

```bash
cd c:\prft-training\shopping-orchestrator
start-all.bat
```

This script:
1. Sets `JAVA_HOME` and `PATH` automatically
2. Opens **7 separate terminal windows**, one per service
3. Starts them in the correct order with delays between each
4. Prints all URLs at the end

> ⏱️ Wait ~2 minutes for all services to fully start.

---

### 🔧 Option B — Manual Start (Step by Step)

Open **7 separate terminal windows** and run each command:

#### Terminal 1 — Eureka Server (START FIRST)
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\eureka-server
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started EurekaServerApplication`
🌐 Open: http://localhost:8761

---

#### Terminal 2 — Product Research Agent
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\product-research-agent
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started ProductResearchApplication on port 8082`

---

#### Terminal 3 — Comparison Agent
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\comparison-agent
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started ComparisonApplication on port 8083`

---

#### Terminal 4 — Budget Agent
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\budget-agent
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started BudgetApplication on port 8084`

---

#### Terminal 5 — Recommendation Agent
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\recommendation-agent
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started RecommendationApplication on port 8085`

---

#### Terminal 6 — Orchestrator Service (START AFTER ALL AGENTS)
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\orchestrator-service
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started OrchestratorApplication on port 8081`

---

#### Terminal 7 — API Gateway (START LAST)
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\prft-training\shopping-orchestrator\backend\api-gateway
C:\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
✅ Wait for: `Started ApiGatewayApplication on port 8090`

---

#### Terminal 8 — Angular Frontend
```bash
cd c:\prft-training\shopping-orchestrator\frontend
node_modules\.bin\ng.cmd serve
```
✅ Wait for: `Local: http://localhost:4200/`
🌐 Open: http://localhost:4200

---

### ⚠️ Startup Order (IMPORTANT)

```
1. eureka-server     ← MUST be first
2. product-research-agent
3. comparison-agent
4. budget-agent
5. recommendation-agent
6. orchestrator-service  ← MUST be after all agents
7. api-gateway           ← MUST be last backend service
8. frontend (Angular)    ← Can start anytime
```

---

## 🌐 All URLs & Endpoints

| Service | URL | Description |
|---------|-----|-------------|
| **Angular UI** | http://localhost:4200 | Main application dashboard |
| **Eureka Dashboard** | http://localhost:8761 | Service registry — see all registered services |
| **API Gateway** | http://localhost:8090 | Entry point for all API calls |
| **Orchestrator** | http://localhost:8081 | Main orchestrator service |
| **H2 Console** | http://localhost:8081/h2-console | In-memory database browser |
| **Research Agent** | http://localhost:8082 | Product research microservice |
| **Comparison Agent** | http://localhost:8083 | Product comparison microservice |
| **Budget Agent** | http://localhost:8084 | Budget analysis microservice |
| **Recommendation Agent** | http://localhost:8085 | Recommendation microservice |

---

## 📡 API Reference

### Main Search (via API Gateway)

#### POST `/api/orchestrator/search`
Triggers the full AI pipeline — research → compare → budget → recommend.

**URL:** `http://localhost:8090/api/orchestrator/search`

**Request Body:**
```json
{
  "productCategory": "Smartphone",
  "budget": 30000,
  "preferences": "camera quality, battery life",
  "brand": "",
  "additionalRequirements": ""
}
```

**Response:** Full `OrchestratorResponse` (see Data Models section)

**Status Codes:**
- `200 OK` — Success
- `400 Bad Request` — Missing required fields
- `500 Internal Server Error` — Agent failure

---

#### GET `/api/orchestrator/history`
Returns list of past searches saved in H2 database.

**URL:** `http://localhost:8090/api/orchestrator/history`

**Response:**
```json
[
  {
    "id": 1,
    "requestId": "uuid-xxxx",
    "productCategory": "Smartphone",
    "budget": 30000,
    "preferences": "camera quality",
    "bestProductName": "Samsung Galaxy S23",
    "bestProductPrice": 29999,
    "createdAt": "2026-06-01T10:00:00",
    "status": "SUCCESS"
  }
]
```

---

#### GET `/api/orchestrator/health`
Health check for the orchestrator service.

**URL:** `http://localhost:8090/api/orchestrator/health`

**Response:**
```json
{ "status": "UP" }
```

---

### Individual Agent Endpoints (Direct Access)

#### Research Agent
```http
GET http://localhost:8082/api/research/products?productType=Smartphone&category=Electronics&budget=30000
```

#### Comparison Agent
```http
POST http://localhost:8083/api/comparison/compare
Content-Type: application/json

[ ...array of ProductInfo objects... ]
```

#### Budget Agent
```http
POST http://localhost:8084/api/budget/analyze?budget=30000
Content-Type: application/json

[ ...array of ProductInfo objects... ]
```

#### Recommendation Agent
```http
POST http://localhost:8085/api/recommendation/recommend?budget=30000&preferences=camera
Content-Type: application/json

{ ...ComparisonResult + BudgetAnalysis... }
```

---

## 🗄️ Database

The **Orchestrator Service** uses an **H2 in-memory database** — no installation required.

### Connection Details
| Property | Value |
|----------|-------|
| JDBC URL | `jdbc:h2:mem:orchestratordb` |
| Driver | `org.h2.Driver` |
| Username | `sa` |
| Password | `password` |
| Console URL | http://localhost:8081/h2-console |

### Accessing H2 Console
1. Open http://localhost:8081/h2-console
2. Enter JDBC URL: `jdbc:h2:mem:orchestratordb`
3. Username: `sa`
4. Password: `password`
5. Click **Connect**

### SearchHistory Table Schema
```sql
CREATE TABLE SEARCH_HISTORY (
    ID              BIGINT AUTO_INCREMENT PRIMARY KEY,
    REQUEST_ID      VARCHAR(255),
    PRODUCT_CATEGORY VARCHAR(255),
    BUDGET          DOUBLE,
    PREFERENCES     VARCHAR(255),
    BEST_PRODUCT_NAME VARCHAR(255),
    BEST_PRODUCT_PRICE DOUBLE,
    CREATED_AT      TIMESTAMP,
    STATUS          VARCHAR(50)
);
```

> ⚠️ **Note:** H2 is in-memory — all data is lost when the Orchestrator service restarts. This is by design (`ddl-auto: create-drop`).

---

## 🖥️ Frontend UI Guide

### Search Page
1. **Quick Category Chips** — Click any chip (💻 Laptop, 📱 Smartphone, etc.) to auto-fill the product type
2. **Product Type** — Type the product you want (e.g., "Gaming Laptop", "iPhone 15")
3. **Budget (₹)** — Enter your maximum budget in Indian Rupees
4. **Preferences** — Optional: describe what matters most (e.g., "long battery life, lightweight")
5. Click **🚀 Find Best Products with AI**

### Loading State
- Animated AI orb with spinning ring
- 4 agent step pills light up as each agent completes its task:
  - 🔍 Researching Products
  - ⚖️ Comparing Options
  - 💰 Analyzing Budget
  - 🏆 Generating Recommendation

### Results Page
The results are displayed in 5 cards:

| Card | Content |
|------|---------|
| 🔍 Your Search Requirements | Shows product, budget, preferences |
| 📦 Top Options Found | Product cards with image, price, rating, features, buy buttons |
| ⚖️ Comparison Summary | Feature comparison table + pros/cons |
| 💰 Budget Analysis | Products within budget + cheaper alternatives |
| 🏆 AI Final Recommendation | Best product with full details + buy button |

### History
- Recent searches appear as clickable chips above the search form
- Click any history item to re-run that search

### Proxy Configuration
The Angular dev server proxies `/api/*` requests to `http://localhost:8090` via `proxy.conf.json`:
```json
{
  "/api": {
    "target": "http://localhost:8090",
    "secure": false,
    "changeOrigin": true
  }
}
```

---

## 📬 Postman Collection

Import the collection from:
```
postman/Shopping-Orchestrator-API.postman_collection.json
```

### How to Import
1. Open Postman
2. Click **Import** → **File**
3. Select `Shopping-Orchestrator-API.postman_collection.json`
4. Click **Import**

### Collection Contents
| Folder | Requests |
|--------|----------|
| Health Checks | Verify all 7 services are running |
| Main Orchestrator Search | 5 example searches (Smartphone, Laptop, Headphones, Gaming, TV) |
| Search History | GET history endpoint |
| Individual Agent Tests | Direct calls to each agent |
| Complete End-to-End Flow | Step-by-step demo sequence |

### Example Searches Included
1. 📱 Smartphone under ₹30,000 (camera + battery focus)
2. 💻 Laptop under ₹60,000 (programming use)
3. 🎧 Wireless Headphones under ₹5,000
4. 🎮 Gaming Laptop under ₹80,000
5. 📺 Smart TV under ₹40,000

---

## 🔄 How It Works — End to End Flow

```
Step 1: User opens http://localhost:4200
        └─ Angular loads dashboard.component.html

Step 2: User fills form:
        productCategory = "Smartphone"
        budget = 30000
        preferences = "camera quality"
        └─ Clicks "🚀 Find Best Products with AI"

Step 3: Angular calls:
        POST http://localhost:4200/api/orchestrator/search
        └─ Proxied to → http://localhost:8090/api/orchestrator/search

Step 4: API Gateway (8090) receives request
        └─ Routes to → orchestrator-service (8081) via Eureka lb://

Step 5: OrchestratorService.processRequest() runs:

        5a. Calls Product Research Agent (8082):
            GET /api/research/products?productType=Smartphone&budget=30000
            ← Returns: List<ProductInfo> (5-10 products)

        5b. Calls Comparison Agent (8083):
            POST /api/comparison/compare
            Body: List<ProductInfo>
            ← Returns: ComparisonResult (feature matrix, pros/cons)

        5c. Calls Budget Agent (8084):
            POST /api/budget/analyze?budget=30000
            Body: List<ProductInfo>
            ← Returns: BudgetAnalysis (within-budget list, best value)

        5d. Calls Recommendation Agent (8085):
            POST /api/recommendation/recommend?budget=30000&preferences=camera
            Body: {ComparisonResult + BudgetAnalysis}
            ← Returns: RecommendationResult (best product + reasoning)

Step 6: Orchestrator saves to H2 database:
        INSERT INTO SEARCH_HISTORY (productCategory, budget, ...)

Step 7: Orchestrator builds OrchestratorResponse:
        {
          topOptionsFound: [...],
          comparisonSummary: {...},
          budgetAnalysis: {...},
          finalRecommendation: {...},
          status: "SUCCESS"
        }

Step 8: Response flows back:
        Orchestrator (8081) → API Gateway (8090) → Angular (4200)

Step 9: Angular renders results:
        - Product cards with images
        - Comparison table
        - Budget analysis
        - Final recommendation with buy button
```

---

## ⚙️ Configuration Reference

### Orchestrator Service (`application.yml`)
```yaml
server:
  port: 8081

spring:
  application:
    name: orchestrator-service
  datasource:
    url: jdbc:h2:mem:orchestratordb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop    # recreates schema on restart
    show-sql: true             # logs all SQL queries
  h2:
    console:
      enabled: true
      path: /h2-console

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

agents:
  research:
    url: http://localhost:8082
  comparison:
    url: http://localhost:8083
  budget:
    url: http://localhost:8084
  recommendation:
    url: http://localhost:8085

feign:
  client:
    config:
      default:
        connectTimeout: 10000   # 10 seconds
        readTimeout: 30000      # 30 seconds
        loggerLevel: BASIC

management:
  endpoints:
    web:
      exposure:
        include: "*"            # exposes all actuator endpoints
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.shopping.orchestrator: DEBUG
    feign: DEBUG
```

### Port Summary
| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| Orchestrator Service | 8081 |
| Product Research Agent | 8082 |
| Comparison Agent | 8083 |
| Budget Agent | 8084 |
| Recommendation Agent | 8085 |
| API Gateway | 8090 |
| Angular Frontend | 4200 |

---

## 🛠️ Troubleshooting

### ❌ "Connection refused" on port 8090
**Cause:** API Gateway not started or Eureka not running.
**Fix:**
1. Check Eureka is running: http://localhost:8761
2. Start API Gateway last (after all other services)

### ❌ Angular shows "Failed to process request"
**Cause:** One or more backend services are not running.
**Fix:**
1. Open http://localhost:8761 — verify all 6 services are registered
2. Check each terminal window for errors
3. Restart services in correct order

### ❌ Eureka shows services as DOWN
**Cause:** Services started before Eureka was ready.
**Fix:** Wait 30 seconds and refresh http://localhost:8761. Services re-register automatically.

### ❌ Maven build fails — "JAVA_HOME not set"
**Fix:**
```bash
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set PATH=%JAVA_HOME%\bin;%PATH%
java -version   # should show Java 17
```

### ❌ `npm start` fails — "execution policy" error
**Fix:** Use the `.cmd` version directly:
```bash
cd c:\prft-training\shopping-orchestrator\frontend
node_modules\.bin\ng.cmd serve
```

### ❌ H2 Console login fails
**Fix:** Use exactly:
- JDBC URL: `jdbc:h2:mem:orchestratordb`
- Username: `sa`
- Password: `password`

### ❌ Port already in use
**Fix:** Kill the process using the port:
```bash
# Find process on port 8082
netstat -ano | findstr :8082
# Kill it (replace PID with actual number)
taskkill /PID <PID> /F
```

---

## ✨ Features Summary

| Feature | Details |
|---------|---------|
| 🤖 4 AI Agents | Research, Compare, Budget, Recommend — each specialized |
| 🏗️ Microservices | 7 independent Spring Boot services |
| 🗂️ Service Discovery | Eureka-based — services find each other by name |
| 🚪 API Gateway | Single entry point, load-balanced routing |
| 💾 Search History | Saved to H2 in-memory database, shown in UI |
| 💰 Budget Optimization | Filters within budget, suggests cheaper alternatives |
| ⚖️ Feature Comparison | Side-by-side comparison table for all products |
| 🏆 Final Recommendation | Best product with detailed reasoning |
| 🛒 Platform Support | Amazon & Flipkart product data |
| 🎨 Modern UI | Dark theme, glassmorphism, animations, product images |
| 📱 Responsive | Works on desktop, tablet, and mobile |
| 🔌 CORS Configured | Frontend-backend communication ready out of the box |
| 📬 Postman Collection | Ready-to-use API test collection included |
| 🔍 Quick Categories | One-click category chips in search form |
| 📊 Actuator | Health & metrics endpoints on all services |

---

## 👨‍💻 Development Notes

- **Angular** uses **standalone components** (no NgModule) — Angular 17 style
- **Feign clients** in Orchestrator use `@FeignClient` annotations for clean inter-service HTTP calls
- **H2 database** uses `create-drop` — schema is auto-created on startup and dropped on shutdown
- **Eureka** uses `prefer-ip-address: true` — services register with IP instead of hostname
- **API Gateway** uses `lb://` URIs — load-balanced via Eureka service names
- **Angular proxy** (`proxy.conf.json`) forwards `/api/*` to `http://localhost:8090` during development

---

*Built with ❤️ using Spring Boot Microservices + Angular 17*
