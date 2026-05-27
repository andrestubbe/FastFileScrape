# FastFileScrape v0.1.0 — Initial Release 🚀

## 🎉 Version 0.1.0: First Public Release
**Release Date:** 2026-05-27
**Tag:** `v0.1.0`

---

## ✨ Features

### ⚡ Native-Accelerated Directory Traversal
- Powered by **FastGLOB** — native Win32 directory walking via JNI.
- Completely bypasses Java NIO's slow `Files.find` + `PathMatcher` stack.
- Gitignore-compatible glob patterns: `**/*.java`, `src/**/*.{cpp,h}`, negation support.

### 🚀 Parallel File Content Extraction
- Multi-threaded `parallelStream()` reads and chunks files simultaneously.
- Thread-safe output with `synchronized` and `AtomicInteger` guards.
- Fast-path string exclusions bypass regex `PathMatcher` for near-zero overhead.

### 🖥️ CLI Tool (`fastfilescrape`)
- Modes: `tree` (structure only), `content` (file contents), `all` (both).
- Output: human-readable text or compact JSONL for AI pipelines.
- Flags: `--ext`, `--include`, `--exclude`, `--stats`, `--out`, `--format`.

### 📦 Fat-JAR Distribution
- All native JNI dependencies (`FastGLOB`, `FastCore`) bundled in one JAR.
- JitPack-ready — single Maven/Gradle dependency, no manual DLL setup.

---

## 📊 Benchmark

| Method | Avg Time |
|---|---|
| Standard Java NIO | ~190 ms |
| FastFileScrape v0.1.0 | ~80 ms |
| **Speedup** | **2.39x faster 🚀** |

---

## 📦 Installation (JitPack)

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastFileScrape</artifactId>
        <version>v0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastGLOB</artifactId>
        <version>v0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

---

## 🔧 Technical Details
- **Architecture:** Hybrid JNI — C++ traversal (FastGLOB) + Java parallel processing.
- **Platform:** Windows 10/11 (Linux planned).
- **Java:** 17+
- **Build:** Maven + maven-assembly-plugin (Fat-JAR).

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀*
