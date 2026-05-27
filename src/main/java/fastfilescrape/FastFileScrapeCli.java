package fastfilescrape;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class FastFileScrapeCli {

    private enum Mode { TREE, CONTENT, ALL }
    private enum Format { TEXT, JSONL }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || hasArg(args, "--help", "-h")) {
            printHelp();
            return;
        }

        long startTime = System.nanoTime();

        Mode mode = parseMode(args);
        Path root = Paths.get(getArgValue(args, "--root", "."));
        List<String> includes = getMulti(args, "--include");
        
        String extValue = getArgValue(args, "--ext", null);
        if (extValue != null) {
            for (String ext : extValue.split(",")) {
                includes.add("**/*." + ext.trim());
            }
        }
        if (includes.isEmpty()) includes = List.of("**/*.java", "**/*.cpp");

        List<String> excludes = getMulti(args, "--exclude");
        String outPath = getArgValue(args, "--out", "-");
        Format format = "jsonl".equalsIgnoreCase(getArgValue(args, "--format", "text"))
                ? Format.JSONL : Format.TEXT;

        int maxChunkBytes = Integer.parseInt(getArgValue(args, "--max-chunk-bytes", "64000"));
        long maxFileSize = Long.parseLong(getArgValue(args, "--max-file-size", "5000000"));
        boolean showStats = hasArg(args, "--stats");
        boolean pretty = hasArg(args, "--pretty", "--prettyjson");

        Writer out = "-".equals(outPath)
                ? new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))
                : Files.newBufferedWriter(Paths.get(outPath), StandardCharsets.UTF_8);

        Set<Path> uniqueFiles = new HashSet<>();
        final int[] chunkCount = {0};
        final long[] totalBytes = {0};

        try (out) {
            if (mode == Mode.TREE || mode == Mode.ALL) {
                FastFileTree.Config tcfg = new FastFileTree.Config();
                tcfg.root = root;
                tcfg.includeGlobs = includes;
                tcfg.excludeGlobs = excludes;

                FastFileTree.Node tree = FastFileTree.build(tcfg);
                if (format == Format.TEXT) {
                    out.write("TREE:\n");
                    FastFileTree.printTree(tree, out);
                    out.write("\n");
                } else {
                    if (pretty) {
                        out.write(toPrettyTreeJson(tree, ""));
                        out.write("\n");
                    } else {
                        writeTreeJson(tree, out);
                    }
                }
            }

            if (mode == Mode.CONTENT || mode == Mode.ALL) {
                FastFileScrapeContent.Config ccfg = new FastFileScrapeContent.Config();
                ccfg.root = root;
                ccfg.includeGlobs = includes;
                ccfg.excludeGlobs = excludes;
                ccfg.maxChunkBytes = maxChunkBytes;
                ccfg.maxFileSizeBytes = maxFileSize;

                if (format == Format.TEXT) {
                    FastFileScrapeContent.scrape(ccfg, (file, chunkIndex, content) -> {
                        synchronized (out) {
                            uniqueFiles.add(file);
                            chunkCount[0]++;
                            totalBytes[0] += content.length();
                            
                            out.write("=== " + root.relativize(file) + " (chunk " + chunkIndex + ") ===\n");
                            out.write(content.toString());
                            out.write("\n\n");
                        }
                    });
                } else {
                    FastFileScrapeContent.scrape(ccfg, (file, chunkIndex, content) -> {
                        synchronized (out) {
                            uniqueFiles.add(file);
                            chunkCount[0]++;
                            totalBytes[0] += content.length();

                            String json = toJsonLine(root.relativize(file).toString(), chunkIndex, content.toString());
                            if (pretty) {
                                json = toPrettyJsonLine(root.relativize(file).toString(), chunkIndex, content.toString());
                            }
                            out.write(json);
                            out.write("\n");
                        }
                    });
                }
            }
        }

        if (showStats) {
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            double mbProcessed = totalBytes[0] / (1024.0 * 1024.0);
            double speed = elapsedMs > 0 ? (mbProcessed / (elapsedMs / 1000.0)) : 0.0;
            
            System.err.println("⚡ [FastFileScrape Stats]");
            System.err.printf("📁 Files processed:   %d\n", uniqueFiles.size());
            System.err.printf("🧩 Chunks generated:  %d\n", chunkCount[0]);
            System.err.printf("💾 Total data read:   %.2f MB\n", mbProcessed);
            System.err.printf("⏱️ Time elapsed:      %d ms\n", elapsedMs);
            if (speed > 0) {
                System.err.printf("🚀 Performance:       %.2f MB/s\n", speed);
            }
            System.err.println();
        }
    }

    private static void printHelp() {
        System.out.println("""
            fastfilescrape [mode] --root <path> [options]

            Modes:
              tree              Structure-only file tree
              content           File contents only
              all               Tree + contents

            Options:
              --root <path>                 Start directory (default: .)
              --include <glob>              Include pattern (repeatable)
              --exclude <glob>              Exclude pattern (repeatable)
              --ext <ext1,ext2>             Include extension shorthand (comma separated)
              --out <file|->                Output file or - for stdout (default: -)
              --format <text|jsonl>         Output format (default: text)
              --max-chunk-bytes <int>       Chunk size in bytes (default: 64000)
              --max-file-size <long>        Max file size in bytes (default: 5000000)
              --stats                       Show execution metrics to stderr
              --pretty                      Human-readable pretty formatted JSON/JSONL output

            Examples:
              fastfilescrape tree --root . --ext java,cpp --stats
              fastfilescrape content --root . --ext java --out repo.txt --stats
              fastfilescrape all --root . --ext java,cpp --format jsonl --pretty --out repo.jsonl
            """);
    }


    private static Mode parseMode(String[] args) {
        if (args.length == 0) return Mode.ALL;
        String m = args[0].toLowerCase(Locale.ROOT);
        return switch (m) {
            case "tree" -> Mode.TREE;
            case "content" -> Mode.CONTENT;
            case "all" -> Mode.ALL;
            default -> Mode.ALL;
        };
    }

    private static boolean hasArg(String[] args, String... keys) {
        for (String a : args) {
            for (String k : keys) {
                if (a.equals(k)) return true;
            }
        }
        return false;
    }

    private static String getArgValue(String[] args, String key, String def) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) return args[i + 1];
        }
        return def;
    }

    private static List<String> getMulti(String[] args, String key) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) {
                list.add(args[i + 1]);
            }
        }
        return list;
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String toJsonLine(String path, int chunkIndex, String content) {
        return "{\"path\":\"" + escapeJson(path) + "\"," +
               "\"chunk\":" + chunkIndex + "," +
               "\"content\":\"" + escapeJson(content) + "\"}";
    }

    private static String toPrettyJsonLine(String path, int chunkIndex, String content) {
        return "{\n" +
               "  \"path\": \"" + escapeJson(path) + "\",\n" +
               "  \"chunk\": " + chunkIndex + ",\n" +
               "  \"content\": \"" + escapeJson(content) + "\"\n" +
               "}";
    }

    private static void writeTreeJson(FastFileTree.Node node, Appendable out) throws IOException {
        out.append("{");
        out.append("\"path\":\"").append(escapeJson(node.path.toString().replace("\\", "/"))).append("\",");
        out.append("\"dir\":").append(String.valueOf(node.isDirectory)).append(",");
        out.append("\"size\":").append(String.valueOf(node.sizeBytes)).append(",");
        out.append("\"children\":[");
        for (int i = 0; i < node.children.size(); i++) {
            if (i > 0) out.append(",");
            writeTreeJson(node.children.get(i), out);
        }
        out.append("]}");
    }

    private static String toPrettyTreeJson(FastFileTree.Node node, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"path\": \"").append(escapeJson(node.path.toString().replace("\\", "/"))).append("\",\n");
        sb.append(indent).append("  \"dir\": ").append(node.isDirectory).append(",\n");
        sb.append(indent).append("  \"size\": ").append(node.sizeBytes).append(",\n");
        sb.append(indent).append("  \"children\": [\n");
        for (int i = 0; i < node.children.size(); i++) {
            sb.append(toPrettyTreeJson(node.children.get(i), indent + "    "));
            if (i < node.children.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
        }
        sb.append(indent).append("  ]\n");
        sb.append(indent).append("}");
        return sb.toString();
    }
}
