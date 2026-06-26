# AI Learning Features — Integration Plan

This document identifies **non-invasive extension points** for future AI-powered learning features. No services are implemented here; the goal is to add capabilities **alongside** the existing visualizer without changing its execution semantics.

## Principles

1. **Observe, don't intercept** — AI services consume execution events and context; they must not alter `ExecutionEngine` control flow.
2. **Reuse existing models** — Build on `StepListener`, `StateSnapshot`, parsed lines, and `FlowchartNode` rather than duplicating state.
3. **UI is additive** — Extend `LearningCenter` or add new tabs/panels; do not refactor `VisualizerUI` stepping logic.
4. **Engine stays pure** — No LLM calls, file I/O, or network access inside `ExecutionEngine`, `CodeParser`, or `Statement` implementations.

## Existing Abstractions to Reuse

| Existing Type | Reuse For |
|---------------|-----------|
| `ExecutionEngine.StepListener` | Real-time step observation (primary hook) |
| `StateSnapshot` | Historical context for explanations and analytics |
| `ExecutionEngine.getVariableStore()` / `getPC()` | Current execution context |
| `CodeParser.getLastParsedLines()` / `getParseSummary()` | Source context without re-parsing |
| `Language` | Language-aware prompts and quiz content |
| `FlowchartNode` | Structural / control-flow context for hints and quizzes |
| `LearningCenter` | Natural home for AI UI (already has example loader callback) |
| `Variable` | Optional typed wrapper if analytics need richer metadata (currently unused; prefer map + DTOs first) |

**Do not duplicate** a second observer protocol. Extend usage of `StepListener` via a composite/chain rather than inventing `ExecutionObserver`, `StepCallback`, etc.

---

## Extension Points (Exact Locations)

### 1. `ExecutionEngine.StepListener` — Primary event bus

**File:** `src/visualizer/ExecutionEngine.java` (lines 15–20, 74, 90–96, 113)

Callbacks already provide everything needed per step:

| Callback | AI Use |
|----------|--------|
| `onStepStart(pc, line)` | Trigger live explanations, pre-step hints |
| `onStepEnd(pc, line, state)` | Post-step analysis, variable-change narration |
| `onError(pc, line, message)` | Targeted error explanations, remediation hints |
| `onExecutionComplete(totalSteps, variablesCount)` | Session summary, quiz generation, analytics flush |

**Integration pattern (future, minimal change to `VisualizerUI.setupCallbacks()` only):**

```java
// Pseudocode — composite listener; engine code unchanged
StepListener aiBridge = new AiStepListenerBridge(analytics, explanation, hint);
StepListener uiListener = /* existing anonymous listener */;
engine.setStepListener(StepListenerChain.of(uiListener, aiBridge));
```

The engine already supports a single listener. A tiny `StepListenerChain` utility (new file) can fan out events without modifying `ExecutionEngine` internals.

### 2. `VisualizerUI.resetAndStart()` — Session boundary

**File:** `src/visualizer/VisualizerUI.java` (lines 272–301)

Fires when a new program is parsed and prepared. AI services should treat this as **session start**:

- Inputs: raw editor text, `linesToExecute`, `Language`, `FlowchartNode` root
- Actions: reset analytics session, preload quiz context, cache parse summary

Hook location: after `engine.prepare(...)` — call `learningServices.onSessionStart(sessionContext)` from UI only.

### 3. `LearningCenter` — AI presentation surface

**File:** `src/visualizer/LearningCenter.java`

Already accepts `Consumer<String> codeLoader` for examples. Extend with:

- Explanation panel (step-synced or on-demand)
- Hint panel (progressive disclosure)
- Quiz panel (post-run or inline)
- Analytics dashboard (session stats)

No changes required to the example-button flow; add sibling panels or sub-tabs.

### 4. Read-only execution context accessors

**File:** `src/visualizer/ExecutionEngine.java`

| Method | AI Use |
|--------|--------|
| `getPC()` | Correlate with editor line and flowchart node |
| `getVariableStore()` | Variable state for explanations |
| `getVariableStateString()` | Compact state for LLM prompts |
| `history` (not exposed today) | **Future optional** `getHistory()` for step replay — add getter only, no behavior change |

### 5. `CodeParser` — Parse artifacts

**File:** `src/visualizer/CodeParser.java`

| Method | AI Use |
|--------|--------|
| `getLastParsedLines()` | Canonical line list aligned with PC |
| `getParseSummary()` | Human/LLM-readable program outline |

Parser output is stable for the duration of a session after `resetAndStart()`.

### 6. `FlowchartGenerator` / `FlowchartNode` — Structure context

**Files:** `src/visualizer/FlowchartGenerator.java`, `FlowchartNode.java`

The flowchart tree maps `lineIndex` → control structure. Use for:

- "You are inside a `while` loop" hints
- Branch-coverage quiz questions
- Path prediction exercises

Access via the `FlowchartNode root` already created in `VisualizerUI.resetAndStart()`; pass into session context DTO.

### 7. `ErrorHandlingTester` — Non-UI test harness

**File:** `src/visualizer/ErrorHandlingTester.java`

Can drive `ExecutionEngine` headlessly for unit-testing AI services against error paths without Swing.

---

## Recommended Package Layout (Future)

Add new types under the same source tree without restructuring the project:

```
src/visualizer/
├── learning/                    # NEW — AI learning domain
│   ├── model/
│   │   ├── ExecutionSessionContext.java
│   │   ├── StepEvent.java
│   │   └── QuizQuestion.java
│   ├── ExplanationService.java
│   ├── LearningAnalyticsService.java
│   ├── HintService.java
│   ├── QuizService.java
│   ├── impl/                    # Concrete / LLM / stub implementations
│   └── StepListenerBridge.java  # Fan-out StepListener adapter
└── (existing files unchanged)
```

---

## Recommended Service Interfaces

These interfaces are designed to compose with existing types. Implementations live in `learning.impl` (stubs, rule-based, or LLM-backed).

### Shared context DTO (new, not a service)

```java
package visualizer.learning.model;

import visualizer.FlowchartNode;
import visualizer.Language;
import java.util.List;
import java.util.Map;

/** Immutable snapshot passed to all learning services at session boundaries. */
public final class ExecutionSessionContext {
    private final String sourceCode;
    private final List<String> parsedLines;
    private final Language language;
    private final FlowchartNode flowchartRoot;
    private final String sessionId;

    // constructor, getters
}
```

```java
package visualizer.learning.model;

import visualizer.StateSnapshot;
import java.util.Map;

/** Normalized step payload — built from StepListener args + engine getters. */
public final class StepEvent {
    private final int pc;
    private final String line;
    private final Map<String, Integer> variablesBefore; // from StateSnapshot
    private final Map<String, Integer> variablesAfter;
    private final String errorMessage; // null if success
    private final boolean isError;

    // constructor, getters
}
```

---

### `ExplanationService`

Generates human-readable narration for the current or completed step.

```java
package visualizer.learning;

import visualizer.learning.model.ExecutionSessionContext;
import visualizer.learning.model.StepEvent;
import java.util.Optional;

public interface ExplanationService {

    /** Called on session start to preload/cache program-level summary (optional). */
    void onSessionStart(ExecutionSessionContext context);

    /** Explain what is about to happen when a step starts. */
    Optional<String> explainStepStart(ExecutionSessionContext context, StepEvent event);

    /** Explain what changed after a step completes. */
    Optional<String> explainStepEnd(ExecutionSessionContext context, StepEvent event);

    /** Explain a runtime error in student-friendly terms. */
    Optional<String> explainError(ExecutionSessionContext context, StepEvent event);

    /** Summarize the full run for the learning tab. */
    Optional<String> summarizeExecution(ExecutionSessionContext context, int totalSteps, int variableCount);

    void onSessionEnd(ExecutionSessionContext context);
}
```

**Wiring:** `StepListenerBridge` invokes `explainStepStart/End/Error` and `summarizeExecution` on `onExecutionComplete`. UI displays results in `LearningCenter` or a side panel.

---

### `LearningAnalyticsService`

Tracks engagement and learning signals without affecting execution.

```java
package visualizer.learning;

import visualizer.learning.model.ExecutionSessionContext;
import visualizer.learning.model.StepEvent;
import java.util.Map;

public interface LearningAnalyticsService {

    void onSessionStart(ExecutionSessionContext context);

    void recordStep(ExecutionSessionContext context, StepEvent event);

    void recordError(ExecutionSessionContext context, StepEvent event);

    void recordHintRequested(ExecutionSessionContext context, int pc, String hintLevel);

    void recordQuizAnswer(ExecutionSessionContext context, String questionId, boolean correct);

    void onSessionEnd(ExecutionSessionContext context);

    /** Returns aggregated metrics for the current or last session. */
    Map<String, Object> getSessionMetrics();

    /** Optional persistence hook — file/DB left to implementation. */
    void flush();
}
```

**Metrics to capture (examples):** steps taken, back-steps used, errors per line, time-on-step (UI timestamps), hints consumed, quiz score.

**Wiring:** Same `StepListenerBridge`; UI buttons in `LearningCenter` call `recordHintRequested` / `recordQuizAnswer`.

---

### `HintService`

Progressive, context-aware hints tied to PC and variable state.

```java
package visualizer.learning;

import visualizer.learning.model.ExecutionSessionContext;
import visualizer.learning.model.StepEvent;
import java.util.Optional;

public interface HintService {

    enum HintLevel { NUDGE, GUIDE, REVEAL }

    void onSessionStart(ExecutionSessionContext context);

    /**
     * Returns the next hint for the current position.
     * Implementations should escalate detail based on prior requests (tracked via analytics).
     */
    Optional<String> requestHint(ExecutionSessionContext context, StepEvent event, HintLevel level);

    /** Proactive hint when user is stuck (e.g., no progress for N seconds). */
    Optional<String> suggestHintIfStuck(ExecutionSessionContext context, StepEvent event, long idleMillis);

    void onSessionEnd(ExecutionSessionContext context);
}
```

**Wiring:** "Hint" button in `LearningCenter` or toolbar calls `requestHint` with current `StepEvent` built from `engine.getPC()`, `linesToExecute`, and `engine.getVariableStore()`. Does not auto-modify code or skip steps.

---

### `QuizService`

Generates and scores questions from the executed program.

```java
package visualizer.learning;

import visualizer.learning.model.ExecutionSessionContext;
import visualizer.learning.model.QuizQuestion;
import java.util.List;

public interface QuizService {

    void onSessionStart(ExecutionSessionContext context);

    /** Generate questions from code structure (pre-run). */
    List<QuizQuestion> generatePreviewQuiz(ExecutionSessionContext context);

    /** Generate questions after execution (variable values, path taken). */
    List<QuizQuestion> generatePostRunQuiz(ExecutionSessionContext context, List<StepEvent> trace);

    /** Score a single answer; may include explanation of correct answer. */
    QuizResult gradeAnswer(ExecutionSessionContext context, QuizQuestion question, String userAnswer);

    void onSessionEnd(ExecutionSessionContext context);

    /** Result DTO */
    final class QuizResult {
        private final boolean correct;
        private final String feedback;
        // constructor, getters
    }
}
```

```java
package visualizer.learning.model;

/** Immutable quiz item. */
public final class QuizQuestion {
    private final String id;
    private final String prompt;
    private final QuestionType type; // MULTIPLE_CHOICE, PREDICT_OUTPUT, FILL_BLANK, TRACE
    private final List<String> choices; // empty for non-MC
    // constructor, getters
}
```

**Wiring:** "Quiz" section in `LearningCenter`; `generatePostRunQuiz` triggered from `onExecutionComplete`. Reuse `FlowchartNode` for branch questions and `StepEvent` trace collected by `StepListenerBridge`.

---

## `StepListenerBridge` (Recommended Adapter)

Single new class to connect engine events to all services without touching `ExecutionEngine`:

```java
package visualizer.learning;

import visualizer.ExecutionEngine.StepListener;
import visualizer.learning.model.ExecutionSessionContext;
import visualizer.learning.model.StepEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements StepListener, builds StepEvent DTOs, delegates to learning services.
 * Registered alongside the existing UI listener via a composite.
 */
public final class StepListenerBridge implements StepListener {
    private final ExecutionSessionContext context;
    private final ExplanationService explanationService;
    private final LearningAnalyticsService analyticsService;
    private final List<StepEvent> trace = new ArrayList<>();

    // onStepStart / onStepEnd / onError / onExecutionComplete implementations
    // build StepEvent, call services, append to trace
}
```

This keeps **all AI side effects out of** `ExecutionEngine` and `VisualizerUI` listener bodies (UI listener remains responsible only for Swing updates).

---

## What Must Not Change

| Component | Reason |
|-----------|--------|
| `ExecutionEngine.executeNextStep()` logic | Core visualizer behavior |
| `CodeParser` cleaning rules | Parse/PC alignment |
| `Statement` implementations | Semantics of execution |
| `ExpressionEvaluator` | Arithmetic/boolean results |
| `FlowchartGenerator` algorithm | Flowchart accuracy |
| Stepping controls / auto-play timing | UX contract |

Allowed minimal touches later (when implementing, not now):

- `VisualizerUI.setupCallbacks()` — register composite `StepListener`
- `VisualizerUI.resetAndStart()` — build `ExecutionSessionContext`, notify services
- `LearningCenter` — add panels wired to service interfaces
- `ExecutionEngine` — optional **getter-only** `getHistory()` if trace replay is needed (no logic change)

---

## Suggested Implementation Phases

| Phase | Scope | Risk |
|-------|-------|------|
| **1 — Observe** | `StepListenerBridge` + `LearningAnalyticsService` stub + trace collection | None — read-only |
| **2 — Explain** | `ExplanationService` stub; display text in `LearningCenter` | None — display only |
| **3 — Hints** | `HintService` + Hint button; analytics for hint levels | None — user-initiated |
| **4 — Quiz** | `QuizService` + post-run quiz UI | None — after execution |
| **5 — AI backend** | Swap stub `impl` for LLM provider behind interfaces | Isolated to `learning.impl` |

---

## Data Flow (Target Architecture)

```
                    ┌─────────────────────┐
                    │    VisualizerUI      │
                    │  (stepping unchanged)│
                    └──────────┬──────────┘
                               │ setStepListener(chain)
                               ▼
                    ┌─────────────────────┐
                    │ StepListenerChain    │
                    └──┬──────────────┬───┘
                       │              │
              ┌────────▼────┐   ┌─────▼──────────────┐
              │ UI Listener  │   │ StepListenerBridge  │
              │ (highlight,  │   │ → ExplanationService│
              │  table, log) │   │ → AnalyticsService  │
              └─────────────┘   │ → HintService       │
                                │ → QuizService       │
                                └─────────────────────┘
                                         │
                                         ▼
                                ┌─────────────────┐
                                │ LearningCenter   │
                                │ (AI panels)      │
                                └─────────────────┘
```

---

## Summary

The visualizer already exposes a clean **observer boundary** (`StepListener`) and **rich context** (parsed lines, PC, variable map, flowchart, snapshots). Future AI learning features should:

1. Introduce `visualizer.learning` service interfaces and DTOs
2. Bridge engine events through `StepListenerBridge` without modifying execution logic
3. Present results in `LearningCenter` (or additional tabs)
4. Reuse `StateSnapshot`, `FlowchartNode`, and `CodeParser` artifacts rather than parallel state machines

No services are implemented in this phase; this plan is the blueprint for additive integration.
