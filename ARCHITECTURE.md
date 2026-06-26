# Code Execution Visualizer — Architecture Summary

A Java Swing desktop application that parses a restricted subset of Java (and partial Python), executes it step-by-step, and visualizes program state, control flow, and execution history.

## Project Layout

```
Code-Execution-Visualizer/
├── src/visualizer/          # All application source (single package)
│   ├── Main.java            # Application entry point
│   ├── VisualizerUI.java    # Primary UI controller
│   ├── CodeParser.java      # Source → cleaned line list
│   ├── ExecutionEngine.java # Step execution & state
│   ├── Statement*.java      # Executable statement types
│   ├── ExpressionEvaluator.java
│   ├── StateSnapshot.java
│   ├── Flowchart*.java      # Control-flow diagram
│   ├── LearningCenter.java  # Static learning tab
│   └── ...
├── bin/                     # Compiled classes (after javac)
└── README.md
```

There is no build tool (Maven/Gradle). The app is compiled and run with `javac` / `java` directly.

## Layered Architecture

The codebase follows a three-layer separation described in the README:

| Layer | Responsibility | Key Classes |
|-------|----------------|-------------|
| **Parser** | Normalize raw editor text into executable lines | `CodeParser`, `Language` |
| **Execution** | Advance program counter, mutate memory, handle control flow | `ExecutionEngine`, `Statement`, `ExpressionEvaluator`, `StateSnapshot` |
| **Presentation** | Editor, stepping controls, variable table, log, flowchart, learning tab | `VisualizerUI`, `FlowchartGenerator`, `FlowchartPanel`, `LearningCenter` |

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  VisualizerUI ──► FlowchartPanel / JTable / JTextArea       │
│       │              LearningCenter                          │
└───────┼─────────────────────────────────────────────────────┘
        │ parseCode()          prepare() / executeNextStep()
        ▼                        ▼
┌───────────────┐        ┌──────────────────┐
│  CodeParser   │        │ ExecutionEngine   │
│  (line list)  │───────►│ + Statements      │
└───────────────┘        │ + ExprEvaluator   │
                         └──────────────────┘
```

## Entry Points

| Entry | Location | Behavior |
|-------|----------|----------|
| **Primary** | `visualizer.Main.main()` | `SwingUtilities.invokeLater` → `new VisualizerUI().setVisible(true)` |
| **Alternate** | `VisualizerUI.main()` | Sets system L&F, then same UI launch |
| **Test utility** | `ErrorHandlingTester.main()` | Console-only error-path checks against `ExecutionEngine` |

Production use: `java -cp bin visualizer.Main`

## End-to-End Execution Flow

1. **User clicks Reset / Restart** → `VisualizerUI.resetAndStart()`
2. **Parse** → `CodeParser.parseCode(code, language)` returns `List<String>` of cleaned lines
3. **Flowchart** → `FlowchartGenerator.generate(lines)` builds a `FlowchartNode` tree; `FlowchartPanel` renders it
4. **Prepare engine** → `ExecutionEngine.prepare(lines, language)` clears memory, resets PC to 0, clears history
5. **Step forward** → `ExecutionEngine.executeNextStep()` (manual, auto-play timer, or after example load)
6. **UI sync** → `ExecutionEngine.StepListener` callbacks update line highlight, log, variable table, flowchart PC
7. **Step back** → `ExecutionEngine.stepBack()` pops a `StateSnapshot` from history and restores PC + variables

```
Editor text
    │
    ▼
CodeParser.parseCode()
    │
    ├──► FlowchartGenerator.generate() ──► FlowchartPanel
    │
    └──► ExecutionEngine.prepare()
              │
              ▼ (each step)
         executeNextStep()
              │
              ├── push StateSnapshot (undo stack)
              ├── StepListener.onStepStart(pc, line)
              ├── execute line / control-flow logic
              ├── StepListener.onStepEnd(pc, line, state)
              └── (on failure) StepListener.onError(...)
```

## How Java Code Is Parsed

Parsing is **two-stage** and **line-oriented** (no AST).

### Stage 1 — `CodeParser` (lexical / structural cleanup)

For each raw line:

1. Split on `\r\n`, `\r`, or `\n`
2. Strip leading/trailing whitespace
3. Remove single-line comments (`//` for Java, `#` for Python)
4. For Java: skip boilerplate (`class`, `main`, `package`, `import` lines)
5. Collapse internal spaces/tabs to a single space
6. Drop blank lines

Output: an ordered, immutable `List<String>` stored in `lastParsedLines`.

### Stage 2 — `ExecutionEngine.parseStatement()` (runtime semantic parse)

Executed lazily on each non-control-flow line during stepping:

| Pattern | Statement Type |
|---------|----------------|
| `int x = expr` (typed LHS) | `DeclarationStatement` |
| `x = expr` | `AssignmentStatement` |
| Other expression | `ExpressionStatement` |
| `if (...)` / `while (...)` | Handled inline by `handleIf` / `handleWhile` |
| `{` / `}` / `pass` | Block boundary / loop back-edge logic |

Java-specific rules:

- Lines must end with `;` (except `{` / `}`)
- Type keywords: `int`, `double`, `float`, `long`, `short`, `byte`
- Conditions extracted from parentheses in `if` / `while` headers

`ExpressionEvaluator` resolves arithmetic (`+ - * /`) and boolean comparisons (`== != >= <= > <`) against the current variable map.

## How Execution Steps Are Generated

A **step** is one call to `executeNextStep()`:

1. If PC ≥ line count → return `false` (done)
2. Skip empty lines and comment-only lines (recursive, no listener events)
3. **Snapshot** current PC and `variableStore` → push onto `history` stack
4. Fire `onStepStart(pc, line)`
5. Dispatch by line prefix:
   - `if` → evaluate condition; enter block or `skipBlock()`
   - `while` → evaluate condition; enter or skip
   - `}` / `pass` → `handleBlockEnd()` (loop back-edge for `while`)
   - Otherwise → `parseStatement(line).execute(variableStore)`; increment PC
6. Fire `onStepEnd(pc - 1, line, variableStore.toString())`
7. On exception → `onError(pc, line, message)`; PC still advances

`stepBack()` pops the most recent `StateSnapshot` and restores PC + variables without re-running the line.

There is **no separate step model object**; steps are implicit in PC movement + listener events + history snapshots.

## State Representation

### Variables / Memory

- **Runtime store**: `LinkedHashMap<String, Integer> variableStore` inside `ExecutionEngine`
- Exposed read-only via `getVariableStore()` → `Map<String, Integer>`
- All `Statement.execute()` implementations receive this map as `memory`
- `Variable.java` exists as a name/value holder but is **not used** by the engine or UI

### Stack

- **Undo history only**: `java.util.Stack<StateSnapshot> history`
- Each `StateSnapshot` copies PC + a deep copy of the variable map
- This is **not** a call stack or operand stack; nested blocks are handled by PC jumps

### Program Counter (PC)

- Integer index into the parsed `List<String>`
- Drives line highlighting (`codeInput` line index) and flowchart node highlight (`FlowchartNode.lineIndex`)

### Output

- **No `System.out` capture** or dedicated output buffer in the engine
- **Execution log**: `JTextArea logArea` in `VisualizerUI`, appended via `StepListener` (`PC n: line`, errors, completion)
- Expression side effects are limited to variable mutation; standalone expressions are evaluated but not printed

### Control-Flow Graph

- `FlowchartNode` tree: `START`, `PROCESS`, `DECISION`, `END` nodes with `lineIndex` mapping back to parsed lines
- Built once at reset; PC overlay updated on each step

## Key Abstractions (Existing)

| Abstraction | Role |
|-------------|------|
| `Statement` | Polymorphic executable line (`execute(HashMap<String,Integer>)`) |
| `ExecutionEngine.StepListener` | Observer for step lifecycle events |
| `StateSnapshot` | Immutable point-in-time execution context |
| `Language` | `JAVA` / `PYTHON` enum for parser and engine branching |
| `LearningCenter` | Swing tab with static tips and example code loader (`Consumer<String>`) |

## Component Inventory

| Class | Purpose |
|-------|---------|
| `Main` | Bootstrap |
| `VisualizerUI` | Layout, controls, orchestration, listener wiring |
| `CodeParser` | Text preprocessing |
| `ExecutionEngine` | Core interpreter loop |
| `DeclarationStatement` | `int x = …` |
| `AssignmentStatement` | `x = …` |
| `ExpressionStatement` | Bare expression |
| `ExpressionEvaluator` | Arithmetic & boolean evaluation |
| `StateSnapshot` | Undo state |
| `FlowchartGenerator` | Line list → graph |
| `FlowchartNode` | Graph node model |
| `FlowchartPanel` | Custom `Graphics2D` renderer |
| `LearningCenter` | Learning tab UI |
| `Language` | Language selector values |
| `Variable` | Unused model class |
| `ErrorHandlingTester` | Manual error-path harness |

## Design Constraints Relevant to Extensions

- Single package (`visualizer`); no module boundaries yet
- Engine is synchronous; UI uses `javax.swing.Timer` for auto-play (800 ms)
- Integer-only values; no strings, arrays, methods, or real JVM execution
- Python support is partial (colon blocks simplified; not full indentation parsing)
