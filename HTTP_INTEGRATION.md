# HTTP Learning API Integration

This document describes how the Java desktop app communicates with the Node.js `learning-api` backend through `HttpLearningApiClient`.

## Overview

```
Learning Services (Explanation, Hint, Quiz, Analytics)
        │
        ▼
LearningApiClient  ← single interface
        │
        ├── MockLearningApiClient     (USE_HTTP_CLIENT=false)
        └── HttpLearningApiClient     (USE_HTTP_CLIENT=true)
                │
                ▼ POST JSON
        learning-api (Node.js Express)
```

The execution engine, parser, and `StepListenerBridge` are unchanged. Only the client implementation selected at startup differs.

## Configuration

| Setting | Source | Default | Description |
|---------|--------|---------|-------------|
| `USE_HTTP_CLIENT` | System property or environment variable | `false` | `true` → HTTP client; `false` → mock client |
| Base URL | `LearningApiConfig.DEFAULT_BASE_URL` | `http://localhost:8080/api/v1` | Backend API prefix |
| Connect timeout | `LearningApiConfig` | 5000 ms | `HttpClient` connect timeout |
| Read timeout | `LearningApiConfig` | 10000 ms | Per-request `HttpRequest` timeout |

### Enable HTTP mode

**Windows (PowerShell)**

```powershell
$env:USE_HTTP_CLIENT="true"
java -cp bin visualizer.Main
```

**Linux / macOS**

```bash
USE_HTTP_CLIENT=true java -cp bin visualizer.Main
```

### Run without backend

Leave `USE_HTTP_CLIENT` unset or set to `false`. The app uses `MockLearningApiClient` and behaves as before.

### Run with backend

1. Start the backend:

   ```bash
   cd learning-api
   npm start
   ```

2. Start the desktop app with `USE_HTTP_CLIENT=true`.

## Request Flow

1. A learning service builds a request DTO (e.g. `ExplainStepRequest`).
2. `HttpLearningApiClient` serializes the DTO to JSON via `LearningApiJson` (no external JSON library).
3. `HttpClient` sends `POST` to `config.resolveUrl("/explain")`, `/hint`, `/quiz`, or `/analyze`.
4. The client parses the standard envelope:

   ```json
   { "success": true, "timestamp": "...", "data": { ... } }
   ```

5. The `data` object is mapped to Java response DTOs.
6. Services convert DTOs into domain models for the Learning Center panel.

### Endpoint mapping

| Client method | HTTP path | Backend route |
|---------------|-----------|---------------|
| `explainStep` | `/explain` | `POST /api/v1/explain` |
| `generateHint` | `/hint` | `POST /api/v1/hint` |
| `generateQuiz` | `/quiz` | `POST /api/v1/quiz` |
| `analyzeSession` | `/analyze` | `POST /api/v1/analyze` |

## Retry Strategy

`HttpLearningApiClient` retries **once** when the first attempt fails with `HttpTimeoutException` (read timeout).

```
POST request
    │
    ├─ success (2xx) → parse response
    │
    ├─ HttpTimeoutException → log retry → POST again
    │       ├─ success → parse response
    │       └─ failure → fallback
    │
    └─ any other failure → fallback
```

Connection timeouts are enforced by `HttpClient.connectTimeout`. Read timeouts are enforced by `HttpRequest.timeout`.

No retry is performed for HTTP 4xx/5xx, connection refused, or parse errors.

## Fallback Behavior

When all attempts fail, `HttpLearningApiClient` returns deterministic local responses from `LearningApiFallback`:

| Operation | Fallback content |
|-----------|------------------|
| Explain | `"A variable named x is created and initialized with value 5."` |
| Hint | `"Remember that variables must be initialized before use."` |
| Quiz | One question: `"What will be the value of x after this line executes?"` |
| Analyze | Accepted with a session fallback message |

**Important:** Fallback responses are returned normally — `HttpLearningApiClient` does not throw to callers. The Learning Center and execution UI keep working.

HTTP failures are logged to **stderr** with the prefix `[LearningAPI-HTTP]`, separate from execution log output.

## Logging

| Event | Log prefix | Example |
|-------|------------|---------|
| HTTP failure | `[LearningAPI-HTTP]` | `explainStep failed: Connection refused` |
| Timeout retry | `[LearningAPI-HTTP]` | `explainStep timed out; retrying once` |
| Fallback used | `[LearningAPI-HTTP]` | `explainStep using local fallback response` |
| Client selection | `[LearningAPI]` | `Using HttpLearningApiClient -> http://localhost:8080/api/v1` |

## Integration Tests

Run against a live backend:

```bash
# Terminal 1
cd learning-api && npm start

# Terminal 2
javac -encoding UTF-8 -d bin -sourcepath src ...
java -cp bin visualizer.learning.api.HttpLearningApiClientIntegrationTest
```

The test class:

1. Verifies **fallback** against an invalid port (no backend required).
2. If `GET /health` returns `UP`, verifies live **explain**, **hint**, **quiz**, and **analyze** responses.

## Package Layout

```
src/visualizer/learning/api/
├── LearningApiClient.java
├── LearningApiClientFactory.java
├── HttpLearningApiClient.java
├── MockLearningApiClient.java
├── LearningApiConfig.java
├── LearningApiFallback.java
├── LearningApiJson.java
├── LearningApiHttpLogger.java
└── HttpLearningApiClientIntegrationTest.java
```

## Future Work

- Externalize base URL via properties file or env var
- Map backend quiz `options` and `answerIndex` into the Learning Center UI
- Surface analyze `strengths` / `weaknesses` in the progress panel
- Circuit breaker after repeated failures
