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


# Show directory tree
fastfilescrape tree --root . --include "**/*.java"

# Extract file contents
fastfilescrape content --root . --include "**/*.java" --out repo.txt

# Tree + Content in JSONL
fastfilescrape all --root . --include "**/*.java" --format jsonl --out repo.jsonl


Installation
Option 1 — JAR
Download fastfilescrape.jar and add it to your classpath.

Option 2 — Maven (JitPack)
xml
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
Demo (Java)
java
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
API Reference
FastFileTree
Method	Description
Node build(Config cfg)	Builds the directory tree
printTree(Node, Appendable)	Prints ASCII tree


FastFileScrapeContent
Method	Description
scrape(Config cfg, Sink sink)	Reads files and emits chunks


Roadmap
See ROADMAP.md.

License
MIT License.

Part of the FastJava Ecosystem
FastFileScrape complements:

FastFileIndex

FastFileSearch

FastFileWatch