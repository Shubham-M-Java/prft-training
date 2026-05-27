# 🛒 Shopping Orchestrator AI

A full-stack AI-powered shopping assistant built with **Spring Boot Microservices** (backend) and **Angular** (frontend).

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Angular UI (port 4200)                    │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                 API Gateway (port 8080)                      │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│            Orchestrator Service (port 8081)                  │
│         [Main Shopping Orchestrator Agent]                   │
└──────┬──────────┬──────────┬──────────────┬─────────────────┘
       │          │          │              │
  ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌──────▼──────┐
  │Research│ │Compare │ │Budget  │ │Recommend    │
  │Agent   │ │Agent   │ │Agent   │ │Agent        │
  │:8082   │ │:8083   │ │:8084   │ │:8085        │
  └────────┘ └────────┘ └────────┘ └─────────────┘
       │          │          │              │
┌──────▼──────────▼──────────▼──────────────▼─────────────────┐
│              Eureka Service Registry (port 8761)             │
└─────────────────────────────────────────────────────────────┘
```

## 📦 Services

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service Discovery & Registry |
| API Gateway | 8080 | Single entry point, routing |
| Orchestrator Service | 8081 | Main agent - coordinates all agents |
| Product Research Agent | 8082 | Finds products based on requirements |
| Comparison Agent | 8083 | Compares products feature-by-feature |
| Budget Agent | 8084 | Filters by budget, finds best value |
| Recommendation Agent | 8085 | Generates final recommendation |
| Angular Frontend | 4200 | UI Dashboard |

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- npm 9+

### Option 1: Start All at Once (Windows)
```bash
cd shopping-orchestrator
start-all.bat
```

### Option 2: Start Manually (in order)

**Step 1: Start Eureka Server**
```bash
cd backend/eureka-server
mvn spring-boot:run
# Wait for: Started EurekaServerApplication
# Open: http://localhost:8761
```

**Step 2: Start All Agents** (open separate terminals)
```bash
cd backend/product-research-agent && mvn spring-boot:run
cd backend/comparison-agent && mvn spring-boot:run
cd backend/budget-agent && mvn spring-boot:run
cd backend/recommendation-agent && mvn spring-boot:run
```

**Step 3: Start Orchestrator**
```bash
cd backend/orchestrator-service
mvn spring-boot:run
```

**Step 4: Start API Gateway**
```bash
cd backend/api-gateway
mvn spring-boot:run
```

**Step 5: Start Angular Frontend**
```bash
cd frontend
npm install
npm start
# Open: http://localhost:4200
```

## 🔌 API Endpoints

### Main Orchestrator (via Gateway: http://localhost:8080)

#### Search for Products
```http
POST /api/orchestrator/search
Content-Type: application/json

{
  "productType": "Smartphone",
  "budget": 30000,
  "preferences": "camera quality, battery life",
  "category": "Smartphones"
}
```

**Response:**
```json
{
  "userRequirement": {
    "productType": "Smartphone",
    "budget": 30000,
    "preferences": "camera quality, battery life",
    "category": "Smartphones"
  },
  "researchedProducts": [...],
  "comparisonResult": {
    "featureComparison": {...},
    "prosAndCons": {...},
    "summary": "..."
  },
  "budgetAnalysis": {
    "productsWithinBudget": [...],
    "cheaperAlternatives": [...],
    "costVsValueInsight": "..."
  },
  "recommendationResult": {
    "bestProduct": {...},
    "reasonForRecommendation": "...",
    "alternativeOptions": [...],
    "finalDecision": "..."
  },
  "processingTimeMs": 245,
  "status": "SUCCESS"
}
```

#### Get Search History
```http
GET /api/orchestrator/history
```

#### Health Check
```http
GET /api/orchestrator/health
```

### Individual Agent Endpoints

| Agent | Endpoint | Method |
|-------|----------|--------|
| Research | `/api/research/products?productType=X&category=Y&budget=Z` | GET |
| Comparison | `/api/comparison/compare` | POST |
| Budget | `/api/budget/analyze?budget=30000` | POST |
| Recommendation | `/api/recommendation/recommend?budget=30000&preferences=camera` | POST |

## 📬 Postman Collection

Import the collection from:
```
postman/Shopping-Orchestrator-API.postman_collection.json
```

The collection includes:
1. **Health Checks** - Verify all services are running
2. **Main Orchestrator Search** - 5 example searches
3. **Search History** - View saved searches
4. **Individual Agent Tests** - Test each agent directly
5. **Complete End-to-End Flow** - Step-by-step demo

### Example Searches in Postman:
- Smartphone under ₹30,000 (camera + battery focus)
- Laptop under ₹60,000 (programming use)
- Wireless Headphones under ₹5,000
- Gaming Laptop under ₹80,000
- Smart TV under ₹40,000

## 🎯 Features

- ✅ **4 Specialized AI Agents** working in coordination
- ✅ **User Preferences Memory** - Search history saved to H2 database
- ✅ **Budget Optimization** - Filters within budget, suggests alternatives
- ✅ **Feature Comparison** - Side-by-side product comparison table
- ✅ **Final Recommendation** - Best product with reasoning
- ✅ **Service Discovery** - Eureka-based microservice registration
- ✅ **API Gateway** - Single entry point with routing
- ✅ **Angular Dashboard** - Beautiful responsive UI
- ✅ **CORS Configured** - Frontend-backend communication ready

## 🗄️ Database

Uses **H2 in-memory database** (no setup required).

H2 Console: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:orchestratordb`
- Username: `sa`
- Password: (empty)

## 🌐 URLs Summary

| URL | Description |
|-----|-------------|
| http://localhost:4200 | Angular UI Dashboard |
| http://localhost:8761 | Eureka Service Registry |
| http://localhost:8080 | API Gateway |
| http://localhost:8081/h2-console | H2 Database Console |
| http://localhost:8081/api/orchestrator/health | Orchestrator Health |

## 📁 Project Structure

```
shopping-orchestrator/
├── backend/
│   ├── pom.xml                          # Parent POM
│   ├── eureka-server/                   # Service Discovery
│   ├── api-gateway/                     # API Gateway
│   ├── orchestrator-service/            # Main Orchestrator Agent
│   ├── product-research-agent/          # Research Agent
│   ├── comparison-agent/                # Comparison Agent
│   ├── budget-agent/                    # Budget Agent
│   └── recommendation-agent/            # Recommendation Agent
├── frontend/                            # Angular 17 App
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/dashboard/    # Main UI Component
│   │   │   ├── models/                  # TypeScript interfaces
│   │   │   └── services/               # HTTP Service
│   │   └── styles.css                  # Global styles
│   ├── package.json
│   └── proxy.conf.json                 # Dev proxy to backend
├── postman/
│   └── Shopping-Orchestrator-API.postman_collection.json
├── start-all.bat                        # Windows startup script
└── README.md
```

## 🔧 Tech Stack

**Backend:**
- Spring Boot 3.2.x
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- Spring Data JPA + H2
- OpenFeign (inter-service communication)
- Lombok

**Frontend:**
- Angular 17 (Standalone Components)
- TypeScript
- RxJS
- CSS3 (custom, no external UI library)

## 💡 How It Works

1. User enters product type, budget, and preferences in the Angular UI
2. Request goes to **API Gateway** → **Orchestrator Service**
3. Orchestrator calls **Product Research Agent** → gets list of products
4. Orchestrator calls **Comparison Agent** → gets feature comparison
5. Orchestrator calls **Budget Agent** → filters within budget
6. Orchestrator calls **Recommendation Agent** → picks best product
7. Orchestrator saves search to H2 database (memory feature)
8. Returns complete structured response to UI
9. UI displays all sections: Products, Comparison, Budget Analysis, Recommendation
