# Learning Layer Architecture

This document describes the additive learning extension built on top of the Code Execution Visualizer. The learning layer observes execution events without modifying parser, engine, or visualization behavior.

## Package Structure

```
src/visualizer/learning/
├── models/          # Immutable data transfer objects
│   ├── ExecutionStep.java
│   ├── Explanation.java
│   ├── Hint.java
│   ├── Quiz.java
│   └── LearningSession.java
├── services/        # Service interfaces and placeholder implementations
│   ├── ExplanationService.java
│   ├── HintService.java
│   ├── QuizService.java
│   ├── LearningAnalyticsService.java
│   ├── PlaceholderExplanationService.java
│   ├── PlaceholderHintService.java
│   └── PlaceholderQuizService.java
├── analytics/       # Analytics-specific implementations
│   └── PlaceholderLearningAnalyticsService.java
└── bridge/          # Engine-to-learning adapters
    ├── StepListenerBridge.java
    └── StepListenerChain.java
```

All learning code lives under `visualizer.learning` and is separate from the core `visualizer` package.

## Event Flow

```
User clicks Reset
       │
       ▼
VisualizerUI.resetAndStart()
       │
       ├── CodeParser.parseCode()
       ├── FlowchartGenerator.generate()
       ├── ExecutionEngine.prepare()
       └── StepListenerBridge.beginSession(LearningSession)
                 │
                 └── services.onSessionStart() + generatePreviewQuiz()

User clicks Step →
       │
       ▼
ExecutionEngine.executeNextStep()
       │
       ▼
StepListenerChain (composite)
       │
       ├── UI StepListener        → highlight, log, variable table
       └── StepListenerBridge     → ExecutionStep → learning services
```

### Per-step callback mapping

| Engine callback | ExecutionStep phase | Bridge actions |
|-----------------|---------------------|----------------|
| `onStepStart` | `STEP_START` | publish step; `explainStepStart` |
| `onStepEnd` | `STEP_END` | publish step; `recordStep`; `explainStepEnd` |
| `onError` | `ERROR` | publish step; `recordError`; `explainError` |
| `onExecutionComplete` | `SESSION_COMPLETE` | publish step; summarize; post-run quiz; `onSessionEnd` on all services |

The bridge reads engine state only through `ExecutionEngine.getVariableStore()` when building `ExecutionStep` snapshots. It never writes to the engine.

## Component Responsibilities

### Models (`learning.models`)

| Class | Responsibility |
|-------|----------------|
| `LearningSession` | Immutable context for one run: session ID, source code, parsed lines, language |
| `ExecutionStep` | Immutable snapshot of one observed event: PC, line, phase, variables, error, timestamp |
| `Explanation` | Text explanation optionally tied to a program counter |
| `Hint` | Hint text with a detail level (`NUDGE`, `GUIDE`, `REVEAL`) |
| `Quiz` | Quiz identifier and a list of `Question` items |

### Services (`learning.services`)

| Interface | Responsibility |
|-----------|----------------|
| `ExplanationService` | Generate explanations for step start/end, errors, and run summaries |
| `HintService` | Provide on-demand or proactive hints for the current step |
| `QuizService` | Generate preview/post-run quizzes and grade answers |
| `LearningAnalyticsService` | Record steps, errors, hint requests, and quiz answers; expose session metrics |

Placeholder implementations return deterministic stub data and perform no I/O:

- `PlaceholderExplanationService` → `"Explanation not implemented."`
- `PlaceholderHintService` → empty hints
- `PlaceholderQuizService` → empty quiz lists
- `PlaceholderLearningAnalyticsService` (in `analytics/`) → in-memory counters only

### Bridge (`learning.bridge`)

| Class | Responsibility |
|-------|----------------|
| `StepListenerBridge` | Implements `ExecutionEngine.StepListener`; converts callbacks into `ExecutionStep` objects and dispatches to injected services |
| `StepListenerChain` | Composite listener that fans out engine callbacks to multiple listeners in order |

### Application wiring (`VisualizerUI`)

`VisualizerUI` creates placeholder services and the bridge via constructor injection, registers `StepListenerChain.of(uiListener, learningBridge)`, and calls `learningBridge.beginSession(...)` after `engine.prepare()` on each reset.

No learning output is displayed in the UI yet; the layer only receives and records events.

## Why the Bridge Pattern

The bridge pattern was chosen for these reasons:

1. **Zero engine changes** — `ExecutionEngine` already exposes `StepListener`. The bridge implements that existing interface rather than adding hooks inside the engine.

2. **Observer, not interceptor** — The bridge listens after the engine fires events. It cannot block, reorder, or alter execution because it runs outside the engine loop.

3. **Composable listeners** — `StepListenerChain` keeps the original UI listener intact and adds the learning bridge as a second delegate. UI behavior is unchanged.

4. **Constructor injection** — Services are passed into `StepListenerBridge` at construction time. Future AI-backed implementations can replace placeholders without changing the bridge or engine.

5. **Clear boundary** — Engine callbacks (`int pc`, `String line`) are normalized into rich `ExecutionStep` DTOs at the boundary, so services never depend on Swing or engine internals.

## Future Integration Points

When AI features are added:

- Replace placeholder service classes with real implementations behind the same interfaces.
- Read `StepListenerBridge.getObservedSteps()` or `LearningAnalyticsService.getSessionMetrics()` from new UI panels in `LearningCenter`.
- Call `HintService.requestHint()` from a Hint button without touching `ExecutionEngine`.

No changes to `ExecutionEngine`, `CodeParser`, or stepping logic are required for those enhancements.
