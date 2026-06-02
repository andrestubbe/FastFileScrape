# FastFileScrape Roadmap

## 🟢 v0.1.0 — Initial Release (Current)
- [x] FastFileTree (with pruning)
- [x] FastFileScrapeContent (chunked file extraction)
- [x] Chunker (byte-aware splitting)
- [x] CLI tool with tree/content/all modes
- [x] JSONL output for AI agents

## 🟡 v0.2.0 — Quality & UX Enhancements
- [ ] Add `--format prettyjson` (group chunks per file)
- [ ] Add `--preview N` to show first N lines of each file
- [ ] Add `--stats` (file count, total bytes, avg chunk size)
- [ ] Add `--ext java,cpp,py` shorthand for include globs
- [ ] Add `--max-depth` for tree limiting
- [ ] Add `--only-dirs` to show only directories containing matches
- [ ] Add `--search "<regex>"` to grep inside files
- [ ] Add `--ignore-defaults` to disable built‑in excludes

## 🟠 v0.5.0 — Performance & Integration
- [ ] Multi-threaded directory walking
- [ ] Memory-mapped file reading (optional)
- [ ] Token-aware chunking (LLM-friendly)
- [ ] Git-ignore integration
- [ ] FastFileIndex integration
- [ ] FastAgent ingestion pipeline

## 🔴 v1.0.0 — Production Hardening
- [ ] Native backend (FastIO)
- [ ] SIMD-accelerated UTF‑8 parser
- [ ] Zero-copy pipeline
- [ ] Live scraping via FastFileWatch
