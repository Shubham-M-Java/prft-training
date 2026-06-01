# 🔭 Observability Guide: Shopping Orchestrator AI

## Overview

Observability = **Metrics** + **Logs** + **Traces**

| Pillar | Tool | Purpose |
|--------|------|---------|
| Metrics | Prometheus + Grafana | Collect & visualize numeric data (CPU, memory, request rates, latency) |
| Logs | ELK Stack / OpenSearch | Centralized log aggregation, search, and analysis |
| Traces | Jaeger | Distributed request tracing across microservices |

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Shopping Orchestrator                         │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Eureka   │  │ API GW   │  │Orchestr. │  │ Agents   │       │
│  │ Server   │  │ :8080    │  │ :8085    │  │:8081-84  │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │             │
│       └──────────────┴──────────────┴──────────────┘            │
│                              │                                   │
│                    ┌─────────▼──────────┐                       │
│                    │  /actuator/metrics  │  (Micrometer)        │
│                    │  /actuator/health   │                       │
│                    │  /actuator/loggers  │                       │
│                    └─────────┬──────────┘                       │
└──────────────────────────────┼──────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐   ┌──────────────────┐   ┌──────────────────┐
│  PROMETHEUS   │   │   ELK / OpenSearch│   │     JAEGER       │
│  (scrapes     │   │   (receives logs  │   │  (receives       │
│   metrics)    │   │    via Logstash)  │   │   traces via     │
│  :9090        │   │   Kibana: :5601   │   │   OTLP/Zipkin)   │
└──────┬────────┘   └──────────────────┘   │  UI: :16686      │
       │                                    └──────────────────┘
       ▼
┌───────────────┐
│    GRAFANA    │
│  Dashboards   │
│  :3000        │
└───────────────┘
```

---

## 📦 STEP 1: Add Dependencies to Each Spring Boot Service

Add the following to **each microservice's `pom.xml`**:

```xml
<!-- Spring Boot Actuator (exposes /actuator endpoints) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus Registry (exposes /actuator/prometheus) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- OpenTelemetry / Micrometer Tracing for Jaeger -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>

<!-- Logstash Logback Encoder (structured JSON logs for ELK) -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

---

## ⚙️ STEP 2: Configure Each Service's `application.yml`

Add to **each service's `application.yml`**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics, loggers
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}  # Labels metrics with service name

# OpenTelemetry tracing → Jaeger
spring:
  application:
    name: orchestrator-service  # Change per service

management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (use 0.1 in production)
  otlp:
    tracing:
      endpoint: http://localhost:4317  # Jaeger OTLP endpoint

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} traceId=%X{traceId} spanId=%X{spanId} - %msg%n"
```

---

## 📝 STEP 3: Configure Structured Logging (Logback)

Create `src/main/resources/logback-spring.xml` in **each service**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Console appender (human-readable for dev) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- JSON appender for Logstash/ELK -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5044</destination>  <!-- Logstash input port -->
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${spring.application.name:-unknown}"}</customFields>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>

</configuration>
```

---

## 🐳 STEP 4: Docker Compose for Observability Stack

Create `shopping-orchestrator/observability/docker-compose.yml`:

```yaml
version: '3.8'

services:

  # ─────────────────────────────────────────
  # PROMETHEUS - Metrics Scraper
  # ─────────────────────────────────────────
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.retention.time=15d'
    networks:
      - observability

  # ─────────────────────────────────────────
  # GRAFANA - Metrics Visualization
  # ─────────────────────────────────────────
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    depends_on:
      - prometheus
    networks:
      - observability

  # ─────────────────────────────────────────
  # ELASTICSEARCH - Log Storage
  # ─────────────────────────────────────────
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - observability

  # ─────────────────────────────────────────
  # LOGSTASH - Log Processor/Shipper
  # ─────────────────────────────────────────
  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    container_name: logstash
    ports:
      - "5044:5044"   # Beats input
      - "5000:5000"   # TCP input (used by logback)
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
    depends_on:
      - elasticsearch
    networks:
      - observability

  # ─────────────────────────────────────────
  # KIBANA - Log Visualization
  # ─────────────────────────────────────────
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - observability

  # ─────────────────────────────────────────
  # JAEGER - Distributed Tracing
  # ─────────────────────────────────────────
  jaeger:
    image: jaegertracing/all-in-one:latest
    container_name: jaeger
    ports:
      - "16686:16686"   # Jaeger UI
      - "4317:4317"     # OTLP gRPC
      - "4318:4318"     # OTLP HTTP
      - "6831:6831/udp" # Jaeger Thrift UDP
      - "14268:14268"   # Jaeger HTTP Thrift
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    networks:
      - observability

volumes:
  grafana-data:
  elasticsearch-data:

networks:
  observability:
    driver: bridge
```

---

## 📊 STEP 5: Prometheus Configuration

Create `shopping-orchestrator/observability/prometheus/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s      # How often to scrape metrics
  evaluation_interval: 15s  # How often to evaluate rules

scrape_configs:

  # Scrape Prometheus itself
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Scrape Eureka Server
  - job_name: 'eureka-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8761']

  # Scrape API Gateway
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']

  # Scrape Orchestrator Service
  - job_name: 'orchestrator-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8085']

  # Scrape Product Research Agent
  - job_name: 'product-research-agent'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8081']

  # Scrape Comparison Agent
  - job_name: 'comparison-agent'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8082']

  # Scrape Budget Agent
  - job_name: 'budget-agent'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8083']

  # Scrape Recommendation Agent
  - job_name: 'recommendation-agent'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8084']
```

---

## 🔄 STEP 6: Logstash Pipeline Configuration

Create `shopping-orchestrator/observability/logstash/pipeline/logstash.conf`:

```
input {
  # Receive JSON logs from Spring Boot services via TCP
  tcp {
    port => 5000
    codec => json_lines
  }
  # Receive logs from Filebeat (optional)
  beats {
    port => 5044
  }
}

filter {
  # Parse the timestamp from the log
  date {
    match => ["@timestamp", "ISO8601"]
  }

  # Add environment tag
  mutate {
    add_field => { "environment" => "development" }
  }

  # If log level is ERROR, tag it
  if [level] == "ERROR" {
    mutate {
      add_tag => ["error"]
    }
  }
}

output {
  # Send to Elasticsearch
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "shopping-orchestrator-%{+YYYY.MM.dd}"
  }

  # Also print to stdout for debugging
  stdout {
    codec => rubydebug
  }
}
```

---

## 📈 STEP 7: Grafana Dashboard Setup

### 7.1 Add Prometheus as Data Source
1. Open Grafana at **http://localhost:3000** (admin/admin)
2. Go to **Configuration → Data Sources → Add data source**
3. Select **Prometheus**
4. Set URL: `http://prometheus:9090`
5. Click **Save & Test**

### 7.2 Import Pre-built Dashboards
Import these dashboard IDs from Grafana.com:

| Dashboard | ID | What it shows |
|-----------|-----|---------------|
| Spring Boot Statistics | **12900** | JVM memory, GC, threads, HTTP requests |
| JVM Micrometer | **4701** | Detailed JVM metrics |
| Spring Boot 3.x | **19004** | Modern Spring Boot metrics |
| Node Exporter Full | **1860** | System-level metrics |

**How to import:**
1. Grafana → **Dashboards → Import**
2. Enter the dashboard ID
3. Select Prometheus as data source
4. Click **Import**

### 7.3 Key Metrics to Monitor

```promql
# HTTP Request Rate (per service)
rate(http_server_requests_seconds_count{application="orchestrator-service"}[5m])

# Average Response Time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Error Rate (5xx responses)
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# JVM Heap Memory Usage
jvm_memory_used_bytes{area="heap"}

# Active HTTP Connections
tomcat_connections_active_current_connections

# CPU Usage
process_cpu_usage
```

---

## 🔍 STEP 8: Kibana Log Analysis Setup

1. Open Kibana at **http://localhost:5601**
2. Go to **Stack Management → Index Patterns**
3. Create index pattern: `shopping-orchestrator-*`
4. Set time field: `@timestamp`
5. Go to **Discover** to search logs

### Useful Kibana Queries (KQL)

```kql
# Find all ERROR logs
level: "ERROR"

# Find logs from a specific service
service: "orchestrator-service"

# Find logs with a specific trace ID (correlate with Jaeger)
traceId: "abc123def456"

# Find slow requests (if you log response times)
message: "slow" AND service: "product-research-agent"

# Find all logs in last 15 minutes with errors
level: "ERROR" AND @timestamp > now-15m
```

---

## 🕵️ STEP 9: Jaeger Distributed Tracing

### How it works:
1. A request enters the **API Gateway**
2. A **Trace ID** is generated and propagated via HTTP headers (`traceparent`)
3. Each service creates a **Span** (child of the parent trace)
4. All spans are sent to **Jaeger Collector**
5. You can visualize the full request journey in **Jaeger UI**

### View Traces:
1. Open Jaeger UI at **http://localhost:16686**
2. Select a **Service** (e.g., `orchestrator-service`)
3. Click **Find Traces**
4. Click on any trace to see the full call chain

### Example Trace Flow:
```
[API Gateway] ──────────────────────────────── 450ms
    └── [Orchestrator Service] ─────────────── 420ms
            ├── [Product Research Agent] ────── 150ms
            ├── [Comparison Agent] ──────────── 100ms
            ├── [Budget Agent] ──────────────── 80ms
            └── [Recommendation Agent] ──────── 90ms
```

---

## 🚀 STEP 10: Running Everything

### Start the Observability Stack:
```bash
cd shopping-orchestrator/observability
docker-compose up -d
```

### Verify all containers are running:
```bash
docker-compose ps
```

### Start your Spring Boot services (they will auto-connect):
```bash
cd shopping-orchestrator
start-all.bat
```

### Access the UIs:
| Tool | URL | Credentials |
|------|-----|-------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | none |
| Kibana | http://localhost:5601 | none |
| Jaeger UI | http://localhost:16686 | none |
| Elasticsearch | http://localhost:9200 | none |

---

## 🔗 STEP 11: Correlating Metrics, Logs, and Traces

The real power of observability is **correlation**:

### Scenario: A slow request is reported

1. **Grafana** shows a spike in response time at 2:30 PM
2. Click the spike → note the **time range**
3. Go to **Kibana** → filter logs for that time range → find `ERROR` or `WARN` logs
4. Note the **traceId** from the log entry
5. Go to **Jaeger** → search by that **traceId**
6. See exactly which microservice was slow and why

### Grafana → Jaeger Integration (Exemplars):
Add to `prometheus.yml` to enable trace exemplars:
```yaml
# In Grafana data source config, set:
# Exemplars → Internal link → Jaeger
# URL: http://localhost:16686/trace/${__value.raw}
```

---

## 📋 STEP 12: Custom Business Metrics (Optional)

Add custom metrics to your services using Micrometer:

```java
// In OrchestratorService.java
@Service
public class OrchestratorService {

    private final MeterRegistry meterRegistry;
    private final Counter searchCounter;
    private final Timer orchestrationTimer;

    public OrchestratorService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.searchCounter = Counter.builder("shopping.searches.total")
            .description("Total number of shopping searches")
            .register(meterRegistry);
        this.orchestrationTimer = Timer.builder("shopping.orchestration.duration")
            .description("Time taken for full orchestration")
            .register(meterRegistry);
    }

    public OrchestratorResponse processRequest(ShoppingRequest request) {
        searchCounter.increment();  // Count each search
        return orchestrationTimer.record(() -> {
            // ... your existing logic
        });
    }
}
```

These custom metrics will automatically appear in Prometheus and Grafana!

---

## 🛡️ Production Considerations

| Concern | Recommendation |
|---------|---------------|
| Sampling Rate | Use 10% tracing in production (`probability: 0.1`) |
| Log Retention | Set Elasticsearch ILM policy (e.g., 30 days) |
| Metrics Retention | Prometheus default 15 days; use Thanos for long-term |
| Alerting | Set up Grafana Alerts for error rate > 1%, latency > 2s |
| Security | Enable Elasticsearch security, use Grafana LDAP/OAuth |
| Resource Usage | ELK stack needs ~4GB RAM minimum |

---

## 📁 Final Directory Structure

```
shopping-orchestrator/
├── observability/
│   ├── docker-compose.yml          ← Start all observability tools
│   ├── prometheus/
│   │   └── prometheus.yml          ← Scrape config for all services
│   ├── grafana/
│   │   └── provisioning/
│   │       ├── datasources/        ← Auto-configure Prometheus
│   │       └── dashboards/         ← Pre-built dashboards
│   └── logstash/
│       └── pipeline/
│           └── logstash.conf       ← Log processing pipeline
├── backend/
│   └── */src/main/resources/
│       ├── application.yml         ← Add actuator + tracing config
│       └── logback-spring.xml      ← Add Logstash appender
└── OBSERVABILITY-GUIDE.md          ← This file
```

---

## ✅ Quick Start Checklist

- [ ] Add `spring-boot-starter-actuator` + `micrometer-registry-prometheus` to all pom.xml
- [ ] Add `logstash-logback-encoder` dependency
- [ ] Update `application.yml` with actuator endpoints exposure
- [ ] Create `logback-spring.xml` with Logstash TCP appender
- [ ] Create `observability/docker-compose.yml`
- [ ] Create `observability/prometheus/prometheus.yml`
- [ ] Create `observability/logstash/pipeline/logstash.conf`
- [ ] Run `docker-compose up -d` in observability folder
- [ ] Start Spring Boot services
- [ ] Import Grafana dashboards (IDs: 12900, 4701)
- [ ] Create Kibana index pattern `shopping-orchestrator-*`
- [ ] Test a request and trace it in Jaeger UI
