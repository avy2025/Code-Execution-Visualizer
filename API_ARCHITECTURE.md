# Learning API Architecture

This document describes the API client layer that sits between the learning service tier and a future REST backend. The visualizer delegates all learning intelligence to this layer through a single interface.

## Layer Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation (VisualizerUI, StepListenerBridge)             │
│  — unchanged stepping / visualization behavior               │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Service Layer (learning.services / learning.analytics)      │
│  ExplanationService, HintService, QuizService,               │
│  LearningAnalyticsService                                    │
│  — map domain models ↔ DTOs, swallow API errors safely       │
└───────────────────────────┬─────────────────────────────────┘
                            │ LearningApiClient (only gateway)
┌───────────────────────────▼─────────────────────────────────┐
│  API Client Layer (learning.api)                             │
│  LearningApiClient → MockLearningApiClient (today)           │
│  LearningApiConfig, LearningApiTimeouts, LearningApiException│
└───────────────────────────┬─────────────────────────────────┘
                            │ future: HTTP + JSON
┌───────────────────────────▼─────────────────────────────────┐
│  REST Backend (not implemented)                              │
│  POST /explain, /hints, /quizzes, /sessions/analyze        │
└─────────────────────────────────────────────────────────────┘
```

## Service Layer

The service layer translates between **domain models** (`LearningSession`, `ExecutionStep`, `Explanation`, `Hint`, `Quiz`) and **API DTOs**. It contains no HTTP logic and no AI SDK calls.

| Class | Role |
|-------|------|
| `PlaceholderExplanationService` | Calls `LearningApiClient.explainStep()` for step start/end, errors, and session summary |
| `PlaceholderHintService` | Calls `LearningApiClient.generateHint()` for on-demand and proactive hints |
| `PlaceholderQuizService` | Calls `LearningApiClient.generateQuiz()` for preview and post-run quizzes |
| `PlaceholderLearningAnalyticsService` | Records local counters; calls `LearningApiClient.analyzeSession()` on `flush()` |

All four services receive the same `LearningApiClient` instance via constructor injection from `VisualizerUI`. API failures are caught as `LearningApiException` and converted to empty optionals or empty lists so the visualizer never breaks.

## API Client Layer

### `LearningApiClient`

The **only** interface through which learning data is requested. Methods:

| Method | Future endpoint | Purpose |
|--------|-----------------|---------|
| `explainStep(ExplainStepRequest)` | `POST {baseUrl}/explain` | Step or session explanations |
| `generateHint(GenerateHintRequest)` | `POST {baseUrl}/hints` | Contextual hints |
| `generateQuiz(GenerateQuizRequest)` | `POST {baseUrl}/quizzes` | Quiz generation |
| `analyzeSession(AnalyzeSessionRequest)` | `POST {baseUrl}/sessions/analyze` | Session analytics upload |

### `MockLearningApiClient`

Current implementation. Returns deterministic fake responses synchronously:

| Response | Mock text |
|----------|-----------|
| Explanation | `"This variable is initialized with value 5."` |
| Hint | `"Check whether the variable has been initialized."` |
| Quiz question | `"What will be the value of x after this line?"` |
| Session analysis | `"Session analysis recorded for {sessionId}"` |

No HTTP, no AI SDK, no local model inference.

### `LearningApiConfig`

Single configuration source for:

| Setting | Default |
|---------|---------|
| Base URL | `http://localhost:8080/api/v1` |
| Connect timeout | 5000 ms |
| Read timeout | 10000 ms |

`resolveUrl("/explain")` builds full URLs for the future HTTP client.

### `LearningApiTimeouts`

Wraps every client call in a synchronous `Future.get(timeout)` using `readTimeoutMs` from config. Throws `LearningApiException` on timeout or interruption. Shared by mock and future HTTP implementations.

### `LearningApiException`

Checked exception for API failures. Propagates from the client; services catch it at the boundary.

### `LearningDtoMapper`

Maps `LearningSession` + `ExecutionStep` domain objects to request DTOs. Keeps DTOs free of Swing or engine dependencies.

## DTO Layer (`learning.api.dto`)

Request and response types mirror the future REST JSON contract.

**Requests**

| DTO | Key fields |
|-----|------------|
| `ExplainStepRequest` | `sessionId`, `sourceCode`, `language`, `StepContextDto`, `Purpose` |
| `GenerateHintRequest` | `sessionId`, `StepContextDto`, `hintLevel`, `idleMillis`, `proactive` |
| `GenerateQuizRequest` | `sessionId`, `parsedLines`, `language`, `QuizType` |
| `AnalyzeSessionRequest` | `sessionId`, `metrics`, `totalSteps`, `variableCount` |
| `StepContextDto` | `pc`, `line`, `phase`, `variables`, `errorMessage` |

**Responses**

| DTO | Key fields |
|-----|------------|
| `ExplainStepResponse` | `text`, `pc` |
| `GenerateHintResponse` | `text`, `level` |
| `GenerateQuizResponse` | `quizId`, `questions` |
| `QuizQuestionDto` | `id`, `prompt` |
| `AnalyzeSessionResponse` | `accepted`, `message` |

DTOs are immutable and serialization-ready for a future JSON mapper.

## Event Flow

```
ExecutionEngine.executeNextStep()
        │
        ▼
StepListenerBridge.onStepEnd(...)
        │
        ├── PlaceholderExplanationService.explainStepEnd()
        │         └── apiClient.explainStep(ExplainStepRequest)
        │
        └── PlaceholderLearningAnalyticsService.recordStep(...)

Session end (onExecutionComplete)
        │
        ├── PlaceholderQuizService.generatePostRunQuiz()
        │         └── apiClient.generateQuiz(GenerateQuizRequest)
        │
        └── PlaceholderLearningAnalyticsService.flush()
                  └── apiClient.analyzeSession(AnalyzeSessionRequest)
```

## Future Backend Communication

To add a real REST client:

1. Implement `LearningApiClient` as `HttpLearningApiClient` (new class, same package).
2. Serialize request DTOs to JSON; `POST` to `config.resolveUrl("/explain")`, etc.
3. Deserialize response DTOs; map HTTP status codes to `LearningApiException`.
4. Reuse `LearningApiTimeouts.execute()` for read-timeout enforcement.
5. Swap the implementation in `VisualizerUI`:

```java
LearningApiClient client = new HttpLearningApiClient(LearningApiConfig.defaults());
```

No changes required to services, bridge, engine, or UI stepping logic.

## Design Constraints

- **One interface** — all remote learning behavior goes through `LearningApiClient`.
- **Synchronous** — blocking calls with timeout; async can be added later behind the same interface.
- **Fail-safe** — services never propagate `LearningApiException` to the UI or engine.
- **No AI in the desktop app** — intelligence lives on the backend; the client only transports DTOs.

## Package Layout

```
src/visualizer/learning/api/
├── LearningApiClient.java
├── LearningApiConfig.java
├── LearningApiException.java
├── LearningApiTimeouts.java
├── LearningDtoMapper.java
├── MockLearningApiClient.java
└── dto/
    ├── ExplainStepRequest.java
    ├── ExplainStepResponse.java
    ├── GenerateHintRequest.java
    ├── GenerateHintResponse.java
    ├── GenerateQuizRequest.java
    ├── GenerateQuizResponse.java
    ├── AnalyzeSessionRequest.java
    ├── AnalyzeSessionResponse.java
    ├── StepContextDto.java
    └── QuizQuestionDto.java
```
