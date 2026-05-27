# FastFileScrape v0.1.0 [ALPHA] — Ultra‑Fast File Tree & Content Scraper for Java

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastFileScrape/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastFileScrape)

**⚡ Scrape and process millions of files in milliseconds with zero latency.**

FastFileScrape is the high‑speed file scraping module of the FastJava ecosystem.  
It provides two core capabilities:

- **FastFileTree** — build complete directory trees with include/exclude rules
- **FastFileScrapeContent** — extract file contents with chunking for LLMs and agents

[![FastFileIndex Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Table of Contents

- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Demo (Java)](#demo-java)
- [API Reference](#api-reference)
- [Roadmap](#roadmap)
- [License](#license)

---

## Key Features

### 🟩 FastFileTree — Directory Structure Engine

- Recursive directory walking
- Include/Exclude glob filters
- Sorted output (folders → files)
- JSON or ASCII tree output
- Git‑ignore aware (optional)

### 🟧 FastFileScrapeContent — File Content Engine

- Extracts file contents with UTF‑8 safety
- Chunking by byte size or newline boundaries
- Include/Exclude patterns
- JSONL or plain text output
- Ideal for LLM context ingestion

### 🟦 CLI Tool — `fastfilescrape`

- `tree` → structure only
- `content` → file contents only
- `all` → both combined
- Output to stdout or file
- JSONL mode for AI pipelines

---

## Quick Start

```pash
# Show directory tree
fastfilescrape tree --root . --include "**/*.java"

# Extract file contents
fastfilescrape content --root . --include "**/*.java" --out repo.txt

# Tree + Content in JSONL
fastfilescrape all --root . --include "**/*.java" --format jsonl --out repo.jsonl
```

---

## Demo (Java)

```java
import fastfilescrape.*;

public class Demo {
    public static void main(String[] args) throws Exception {

        // Tree
        var tcfg = new FastFileTree.Config();
        tcfg.root = Path.of(".");
        var tree = FastFileTree.build(tcfg);
        FastFileTree.printTree(tree, System.out);

        // Content
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
    <artifactId>fastfilescrape</artifactId>
    <version>v0.1.0</version>
</dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.andrestubbe:fastfilescrape:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 *
   *[fastfilescrape-v0.1.0.jar](https://github.com/andrestubbe/FastFileIndex/releases/download/v0.1.0/fastfilescrape-v0.1.0.jar)
   ** (The Core Library)

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

* **[COMPILE.md](COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
* **[REFERENCE.md](REFERENCE.md)**: Full API descriptions, border configurations, and codepoint index.
* **[PHILOSOPHIE.md](PHILOSOPHIE.md)**: The engineering rationale for zero-allocation performance.
* **[ROADMAP.md](ROADMAP.md)**: Future milestones and planned features.

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
