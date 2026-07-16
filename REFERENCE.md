# FastFileScrape Reference

## 1. Modules

### 1.1 FastFileTree
Builds a complete directory tree with include/exclude filtering.

**Guarantees**
- Deterministic ordering
- Zero recursion overflow (iterative walk)
- Minimal allocations

### 1.2 FastFileScrapeContent
Extracts file contents and emits chunks.

**Chunking Rules**
- UTF‑8 safe
- Prefers newline boundaries
- Maximum chunk size configurable

---

## 2. Configuration

### FastFileTree.Config
| Field | Type | Description |
|-------|------|-------------|
| `root` | Path | Root directory |
| `includeGlobs` | List<String> | Include patterns |
| `excludeGlobs` | List<String> | Exclude patterns |

### FastFileScrapeContent.Config
| Field | Type | Description |
|-------|------|-------------|
| `root` | Path | Root directory |
| `includeGlobs` | List<String> | Include patterns |
| `excludeGlobs` | List<String> | Exclude patterns |
| `maxChunkBytes` | int | Max chunk size |
| `maxFileSizeBytes` | long | Skip large files |
| `charset` | Charset | Default UTF‑8 |

---

## 3. CLI Reference

