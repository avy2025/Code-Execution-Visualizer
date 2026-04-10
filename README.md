# Code Execution Visualizer

A Java project that simulates and visualizes the step-by-step execution of Java-like code using Object-Oriented Programming principles.

## Project Structure

```
CodeExecutionVisualizer/
└── src/
    └── visualizer/
        ├── Main.java            # Entry point – reads multi-line user input
        ├── CodeParser.java      # Splits raw code string into trimmed lines
        ├── ExecutionEngine.java # Processes each line and tracks variable state
        └── Variable.java        # Encapsulated model for a variable (name + value)
```

## OOP Principles Used

- **Encapsulation** – All fields are `private` with public getters/setters
- **Single Responsibility** – Each class has one focused role
- **Package Structure** – All classes organized under `visualizer` package

## Requirements

- Java JDK 17+ (e.g. [Eclipse Temurin](https://adoptium.net/))

## How to Compile & Run

```bash
# From the CodeExecutionVisualizer directory

# Compile
javac src/visualizer/*.java

# Run
java -cp src visualizer.Main
```

## Usage

Enter multi-line Java-like code, then type `END` to finish input:

```
int x = 10
String name = "Alice"
double pi = 3.14
END
```

### Sample Output

```
===========================================
         Code Execution Visualizer
===========================================
Enter your multi-line Java-like code.
(Type 'END' on a new line to finish):
-------------------------------------------

--- 1. Parsing Code ---
Parsed 3 lines of code.

--- 2. Executing Code ---
Starting execution...
>> Executing: int x = 10
   [State Update] Stored: Variable{name='x', value=10}
>> Executing: String name = "Alice"
   [State Update] Stored: Variable{name='name', value="Alice"}
>> Executing: double pi = 3.14
   [State Update] Stored: Variable{name='pi', value=3.14}
Execution finished. Final variables in memory: 3
```
