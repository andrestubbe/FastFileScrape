# FastFileScrape 0.1.0 [ALPHA-2026-05-25] — Ultra‑Fast File Tree & Content Scraper for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastFileScrape/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastFileScrape)

**⚡ Scrape and process millions of files in milliseconds with zero latency.**

FastFileScrape is the high‑speed file scraping module of the FastJava ecosystem.  
It provides two core capabilities:

- **FastFileTree** — build complete directory trees with include/exclude rules
- **FastFileScrapeContent** — extract file contents with chunking for LLMs and agents

[![FastFileScrape Showcase](docs/screenshot.png)](https://youtu.be/3yPRjiXqpaY)

---

## Quick Start

```java
import fastfilescrape.*;
import java.nio.file.Path;
import java.util.List;

public class Demo {
    public static void main(String[] args) throws Exception {
        // 1. FastFileTree — Build and print directory tree
        var tcfg = new FastFileTree.Config();
        tcfg.root = Path.of(".");
        var tree = FastFileTree.build(tcfg);
        FastFileTree.printTree(tree, System.out);

        // 2. FastFileScrapeContent — Extract chunked contents for LLMs & agents
        var ccfg = new FastFileScrapeContent.Config();
        ccfg.root = Path.of(".");
        ccfg.includeGlobs = List.of("**/*.java");

        FastFileScrapeContent.scrape(ccfg, (file, chunk, text) -> {
            System.out.println("=== " + file + " (chunk " + chunk + ") ===");
            System.out.println(text);
        });
    }
}
```

### CLI Tool — `fastfilescrape`

```bash
# Show directory tree
fastfilescrape tree --root . --include "**/*.java"

# Extract file contents
fastfilescrape content --root . --include "**/*.java" --out repo.txt

# Tree + Content in JSONL
fastfilescrape all --root . --include "**/*.java" --format jsonl --out repo.jsonl
```

---

## Table of Contents

- [Why FastFileScrape?](#why-fastfilescrape?)
- [Key Features](#key-features)
- [Installation](#installation)
- [API Reference](#api-reference)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastFileScrape?

Java's standard `Files.walk()` and `Files.readString()` work — but they were not designed for scraping millions of files at agent speed.

| Concern | Standard Java | FastFileScrape |
|---|---|---|
| **Directory traversal** | `Files.walk()` — JVM syscall per entry | `FastGLOB` native Win32 traversal — batch results |
| **Glob matching** | `PathMatcher` — regex compiled per match | FastGLOB native — string contains fast-path first |
| **File reading** | Sequential, one file at a time | `parallelStream()` — all files in parallel |
| **Exclude checks** | Full `PathMatcher` regex per file | String `contains()` fast-path, regex only as fallback |
| **LLM chunking** | Manual splitting, easy to split mid-token | Built-in UTF-8 boundary-safe 64 KB chunks |
| **Size guard** | Manual | Configurable `maxFileSizeBytes`, skips binaries |

### The real use case

When an AI agent needs to read an entire codebase into context — say, 2 000 `.java` files across 400 folders — standard Java spends most of its time in filesystem overhead and sequential I/O.  
FastFileScrape does the traversal natively, filters with a string fast-path, and reads all matching files in parallel. The `Sink` callback streams chunks directly to the agent pipeline without buffering the entire repo in memory.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastFileScrape</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastGLOB</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.andrestubbe:FastFileScrape:0.1.0'
    implementation 'com.github.andrestubbe:FastGLOB:0.1.0'
    implementation 'com.github.andrestubbe:FastCore:v1.0.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the pre-compiled JARs to add them to your classpath:

1. 📦 [**FastFileScrape-0.1.0.jar**](https://github.com/andrestubbe/FastFileScrape/releases) (The Scraper Core Library)
2. 📦 [**FastGlob-0.1.0.jar**](https://github.com/andrestubbe/FastGLOB/releases) (The Native Glob Matching Library)
3. ⚙️ [**fastcore-v1.0.0.jar**](https://github.com/andrestubbe/FastCore/releases) (The Mandatory JNI Loader)

> [!IMPORTANT]
> Since FastFileScrape is natively accelerated, all three JARs must be present in your classpath for the JNI-accelerated directory walking to operate correctly on Windows.

---

## API Reference

### FastFileTree

| Method                        | Description               |
|-------------------------------|---------------------------|
| `Node build(Config cfg)`      | Builds the directory tree |
| `printTree(Node, Appendable)` | Prints ASCII tree         |

### FastFileScrapeContent

| Method                          | Description                  |
|---------------------------------|------------------------------|
| `scrape(Config cfg, Sink sink)` | Reads files and emits chunks |

---

## Documentation

* **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
* **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, border configurations, and codepoint index.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-allocation performance.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features.

---

## Platform Support

| Platform      | Status            |
|---------------|-------------------|
| Windows 10/11 | ✅ Fully Supported |
| Linux         | 🚧 Planned        |
| macOS         | 🚧 Planned        |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastFileIndex](https://github.com/andrestubbe/FastFileIndex) — Ultra-fast filesystem scanner
- [FastFileContentIndex](https://github.com/andrestubbe/FastFileContentIndex) — High-speed in-file text indexing
- [FastFileWatch](https://github.com/andrestubbe/FastFileWatch) — High-performance directory watch service using USN Journal
- [FastFileSearch](https://github.com/andrestubbe/FastFileSearch) — Ultra-fast indexed file prefix trie search
- [FastGLOB](https://github.com/andrestubbe/FastGLOB) — Ultra-fast native Win32 glob matching and traversal
- [FastFileSystem](https://github.com/andrestubbe/FastFileSystem) — Unified filesystem operations (Index, Search, Watch, Scrape) in one API

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
