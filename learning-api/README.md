# Learning API

Standalone Node.js backend for the **Java Code Learning Platform**. It exposes REST endpoints consumed by the desktop Code Execution Visualizer learning layer.

Currently returns **deterministic mock responses**. Gemini integration and persistence will be added later.

## Tech Stack

- Node.js
- Express
- JavaScript
- dotenv, cors, helmet, express-validator
- Swagger (OpenAPI 3) via swagger-jsdoc + swagger-ui-express

## Project Structure

```
learning-api/
├── src/
│   ├── index.js              # Application entry point
│   ├── config/               # Environment and Swagger config
│   ├── routes/               # Route definitions + OpenAPI annotations
│   ├── controllers/          # HTTP adapters
│   ├── services/             # Business logic (mock today)
│   ├── middleware/           # Validation, errors, logging
│   ├── prompts/              # Future Gemini prompt templates
│   └── utils/                # Response envelope and errors
├── .env.example
└── package.json
```

## Quick Start

```bash
cd learning-api
npm install
cp .env.example .env   # optional
npm start
```

Server defaults to **http://localhost:8080**.

| Resource | URL |
|----------|-----|
| Health | http://localhost:8080/health |
| Swagger UI | http://localhost:8080/api-docs |
| API base | http://localhost:8080/api/v1 |

## Response Format

### Success

```json
{
  "success": true,
  "timestamp": "2026-06-26T12:00:00.000Z",
  "data": {}
}
```

### Error

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "sessionId is required"
  }
}
```

## Endpoints

### `GET /health`

Health check (not under `/api/v1`).

**Response**

```json
{
  "status": "UP"
}
```

---

### `POST /api/v1/explain`

Explains a single execution step or session summary.

**Request body**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | string | yes | Session identifier |
| `sourceCode` | string | yes | Raw editor source |
| `language` | string | yes | `JAVA` or `PYTHON` |
| `purpose` | string | yes | `STEP_START`, `STEP_END`, `ERROR`, `SESSION_SUMMARY` |
| `step` | object | yes | Step context (see below) |
| `totalSteps` | integer | no | Used for session summary |
| `variableCount` | integer | no | Used for session summary |

**Step object**

| Field | Type | Required |
|-------|------|----------|
| `pc` | integer | yes |
| `line` | string | yes |
| `phase` | string | yes |
| `variables` | object | yes |
| `errorMessage` | string | no |

**Example request**

```bash
curl -X POST http://localhost:8080/api/v1/explain \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "abc-123",
    "sourceCode": "int x = 5;",
    "language": "JAVA",
    "purpose": "STEP_END",
    "step": {
      "pc": 0,
      "line": "int x = 5;",
      "phase": "STEP_END",
      "variables": { "x": 5 }
    }
  }'
```

**Mock `data` response**

```json
{
  "text": "A variable named x is created and initialized with value 5.",
  "pc": 0,
  "purpose": "STEP_END",
  "sessionId": "abc-123"
}
```

---

### `POST /api/v1/hint`

Generates a contextual hint for the current step.

**Request body**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | string | yes | Session identifier |
| `step` | object | yes | Step context |
| `hintLevel` | string | yes | `NUDGE`, `GUIDE`, or `REVEAL` |
| `idleMillis` | integer | no | Idle time for proactive hints |
| `proactive` | boolean | no | Whether hint is proactive |

**Mock `data` response**

```json
{
  "text": "Remember that variables must be initialized before use.",
  "level": "GUIDE",
  "sessionId": "abc-123",
  "proactive": false
}
```

---

### `POST /api/v1/quiz`

Generates a multiple-choice quiz question.

**Request body**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | string | yes | Session identifier |
| `sourceCode` | string | yes | Raw source code |
| `parsedLines` | string[] | yes | Cleaned executable lines |
| `language` | string | yes | `JAVA` or `PYTHON` |
| `quizType` | string | yes | `PREVIEW` or `POST_RUN` |

**Mock `data` response**

```json
{
  "quizId": "abc-123-preview",
  "question": "What will be the value of x after this line executes?",
  "options": ["5", "10", "105", "0"],
  "answerIndex": 2,
  "explanation": "When the condition is true, x is increased by 100, so the final value is 105.",
  "quizType": "PREVIEW",
  "sessionId": "abc-123"
}
```

---

### `POST /api/v1/analyze`

Analyzes a completed session using collected metrics.

**Request body**

| Field | Type | Required |
|-------|------|----------|
| `sessionId` | string | yes |
| `metrics` | object | yes |
| `totalSteps` | integer | yes |
| `variableCount` | integer | yes |

**Mock `data` response**

```json
{
  "sessionId": "abc-123",
  "strengths": ["Clear variable initialization", "Consistent stepping through the program"],
  "weaknesses": ["Limited use of comments", "No error-handling examples yet"],
  "recommendation": "Practice predicting variable values before each step to strengthen mental modeling.",
  "totalSteps": 5,
  "variableCount": 2
}
```

---

### `POST /api/v1/session/start`

Registers the start of a learning session (in-memory only).

**Request body**

| Field | Type | Required |
|-------|------|----------|
| `sessionId` | string | yes |
| `sourceCode` | string | yes |
| `language` | string | yes |
| `parsedLines` | string[] | yes |

**Response `data`**

```json
{
  "sessionId": "abc-123",
  "status": "ACTIVE",
  "message": "Learning session started.",
  "lineCount": 4
}
```

---

### `POST /api/v1/session/end`

Marks a learning session as ended (in-memory only).

**Request body**

| Field | Type | Required |
|-------|------|----------|
| `sessionId` | string | yes |
| `totalSteps` | integer | yes |
| `variableCount` | integer | yes |
| `metrics` | object | no |

**Response `data`**

```json
{
  "sessionId": "abc-123",
  "status": "ENDED",
  "message": "Learning session ended.",
  "totalSteps": 5,
  "variableCount": 2
}
```

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | HTTP port |
| `NODE_ENV` | `development` | Environment |
| `API_PREFIX` | `/api/v1` | API route prefix |
| `CORS_ORIGIN` | `*` | CORS allowed origin |
| `GEMINI_API_KEY` | — | Reserved for future AI integration |

## Architecture Notes

- **Controllers** handle HTTP only and return the standard envelope.
- **Services** contain business logic; mock implementations will be swapped for Gemini calls later.
- **Middleware** validates requests with express-validator before controllers run.
- **prompts/** holds future prompt templates; not used by mocks yet.
- **No database** — session state is stored in memory for development only.

## Future Work

- Replace mock services with Gemini API integration
- Add `HttpLearningApiClient` in the Java desktop app
- Persist sessions and analytics
- Add authentication and rate limiting

## License

MIT
