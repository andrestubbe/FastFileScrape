# Philosophy of FastFileScrape

FastFileScrape follows the same principles as the FastJava ecosystem:

> “No copies. Ever. Critical JNI path. Native‑first performance.”

Even though FastFileScrape v0.1.0 is pure Java, its architecture is designed for a future native backend.

## Core Tenets

### 1. Zero Bloat
Only essential logic. No streams, no lambdas, no unnecessary abstractions.

### 2. Deterministic Behavior
Stable ordering, stable chunking, stable output.

### 3. Hardware‑Aware Design
The Java version mirrors the structure of the future native version.

### 4. AI‑Ready
JSONL output and chunking optimized for LLM ingestion.

### 5. Blueprint Consistency
Matches the structure of FastFileIndex, FastFileSearch, FastFileWatch, etc.

