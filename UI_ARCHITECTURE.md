# Learning Center UI Architecture

This document describes the Swing UI for the dockable Learning Center and how it connects to the learning service layer without affecting code execution.

## Component Hierarchy

```
VisualizerUI (JFrame)
└── learningDockSplit (JSplitPane, horizontal)
    ├── mainPanel (existing visualizer workspace)
    │   ├── header
    │   ├── mainSplit (editor | dashboard tabs)
    │   └── controlPanel (+ Learning Mode button)
    └── LearningCenter (dockable panel, hidden by default)
        └── JScrollPane
            └── sections (BoxLayout Y_AXIS)
                ├── Current Step
                ├── AI Explanation
                ├── Hint
                ├── Quick Quiz
                └── Learning Progress

LearningCenterController
    └── implements LearningPanelListener
        └── updates LearningCenter on EDT via SwingUtilities.invokeLater
```

## Layer Responsibilities

| Layer | Package / Class | Responsibility |
|-------|-----------------|----------------|
| **Shell** | `VisualizerUI` | Layout, Learning Mode toggle, dock visibility, engine stepping (unchanged) |
| **View** | `LearningCenter` | Renders five sections; no service or API calls |
| **Controller** | `LearningCenterController` | Receives bridge events; schedules lightweight EDT updates |
| **Bridge** | `StepListenerBridge` | Observes execution; calls services; builds `LearningPanelSnapshot` |
| **Services** | `learning.services.*` | Delegate to `LearningApiClient` (mock today) |
| **API** | `MockLearningApiClient` | Deterministic fake REST responses |

The UI never calls `LearningApiClient` directly. All learning content flows through `StepListenerBridge`.

## Event Flow

```
User clicks Step
       │
       ▼
ExecutionEngine.executeNextStep()
       │
       ▼
StepListenerChain
       ├── UI StepListener (highlight, log, variables — unchanged)
       └── StepListenerBridge
               ├── ExplanationService / HintService / QuizService / Analytics
               ├── builds LearningPanelSnapshot
               └── LearningPanelListener.onPanelUpdate(snapshot)
                       │
                       ▼
               LearningCenterController (invokeLater)
                       │
                       ▼
               LearningCenter.applySnapshot(snapshot)
```

### Session lifecycle

| Event | Bridge action | UI effect |
|-------|---------------|-----------|
| `resetAndStart()` → `beginSession()` | Preview quiz, `onSessionReset()` | Quiz loaded; reveal answer hidden |
| `onStepStart` | `explainStepStart`, current step fields | Line, statement, variables, explanation |
| `onStepEnd` | `explainStepEnd`, `requestHint`, analytics | Explanation, hint, progress metrics |
| `onError` | `explainError`, error analytics | Explanation fallback, error count |
| `onExecutionComplete` | Summary, post-run quiz, `analyzeSession` | Final explanation, quiz, metrics |
| `stepBackward()` → `syncCurrentStep()` | Engine read only | Current step section refreshed |

## Update Lifecycle

1. **Bridge runs on the EDT** during stepping (same thread as existing UI listener). Service calls use `MockLearningApiClient` and return immediately.
2. **Snapshot is immutable** — `LearningPanelSnapshot` is built once per event in the bridge.
3. **Controller defers paint** — `SwingUtilities.invokeLater` ensures panel mutations happen on the next EDT frame without nesting inside engine callbacks.
4. **Errors are contained** — service failures and UI exceptions show friendly placeholders; execution continues unaffected.

## Learning Mode Toggle

| State | Behavior |
|-------|----------|
| **OFF** (default) | Learning panel hidden (`dividerLocation = 1.0`); visualizer layout unchanged |
| **ON** | Panel docked on the right (~340px); receives live bridge updates |

The bridge always publishes events regardless of toggle state so enabling Learning Mode shows current data immediately.

## Section Data Mapping

| UI Section | Snapshot fields | Source |
|------------|-----------------|--------|
| Current Step | `lineNumber`, `statement`, `variablesText` | Engine PC + parsed line + variable map |
| AI Explanation | `explanationText` | `ExplanationService` → `LearningApiClient.explainStep` |
| Hint | `hintText` | `HintService` → `LearningApiClient.generateHint` (or `"No hint available."`) |
| Quick Quiz | `quizQuestion`, `quizOptions`, `quizAnswer` | `QuizService` → `LearningApiClient.generateQuiz` |
| Learning Progress | `totalSteps`, `hintsGenerated`, `quizzesGenerated`, `errorsEncountered` | `LearningAnalyticsService.getSessionMetrics()` |

Quiz options are deterministic placeholders (`5`, `10`, `105`, `0`) until the backend provides real choices. **Reveal Answer** is local UI only — no scoring.

## Separation of Concerns

- **Execution engine** — unaware of learning UI.
- **StepListenerBridge** — only extension point that couples execution events to learning; publishes snapshots, does not reference Swing.
- **LearningCenter** — passive view; `applySnapshot` only sets labels and text areas.
- **LearningCenterController** — thin adapter between bridge and view.
- **Services / API** — no Swing imports; swappable when a real HTTP client replaces the mock.

## Future UI Work

- Wire **Reveal Answer** and quiz selection to `QuizService.gradeAnswer` when scoring is added.
- Add hint request button calling `HintService.requestHint` on demand.
- Display session analysis message from `analyzeSession` response.
- Optional: skip `invokeLater` when already on EDT if profiling shows unnecessary overhead (not required today).

## Package Layout

```
src/visualizer/
├── VisualizerUI.java
├── LearningCenter.java
└── learning/
    ├── bridge/
    │   ├── StepListenerBridge.java
    │   ├── LearningPanelListener.java
    │   └── LearningPanelSnapshot.java
    └── ui/
        └── LearningCenterController.java
```
