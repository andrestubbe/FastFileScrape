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

        Mode mode = parseMode(args);
        Path root = Paths.get(getArgValue(args, "--root", "."));
        List<String> includes = getMulti(args, "--include");
        List<String> excludes = getMulti(args, "--exclude");
        if (includes.isEmpty()) includes = List.of("**/*.java", "**/*.cpp");

        String outPath = getArgValue(args, "--out", "-");
        Format format = "jsonl".equalsIgnoreCase(getArgValue(args, "--format", "text"))
                ? Format.JSONL : Format.TEXT;

        int maxChunkBytes = Integer.parseInt(getArgValue(args, "--max-chunk-bytes", "64000"));
        long maxFileSize = Long.parseLong(getArgValue(args, "--max-file-size", "5000000"));

        Writer out = "-".equals(outPath)
                ? new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))
                : Files.newBufferedWriter(Paths.get(outPath), StandardCharsets.UTF_8);

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
                    writeTreeJson(tree, out);
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
                        out.write("=== " + root.relativize(file) + " (chunk " + chunkIndex + ") ===\n");
                        out.write(content.toString());
                        out.write("\n\n");
                    });
                } else {
                    FastFileScrapeContent.scrape(ccfg, (file, chunkIndex, content) -> {
                        String json = toJsonLine(root.relativize(file).toString(), chunkIndex, content.toString());
                        out.write(json);
                        out.write("\n");
                    });
                }
            }
        }
    }

    private static void printHelp() {
        System.out.println("""
                fastfilescrape [mode] --root <path> [options]

                Modes:
                  tree              Nur Struktur-Baum
                  content           Nur Datei-Inhalte
                  all               Baum + Inhalte

                Options:
                  --root <path>                 Startverzeichnis (default: .)
                  --include <glob>              Include-Pattern (mehrfach)
                  --exclude <glob>              Exclude-Pattern (mehrfach)
                  --out <file|->                Ausgabe-Datei oder - für stdout (default: -)
                  --format <text|jsonl>         Ausgabeformat (default: text)
                  --max-chunk-bytes <int>       Chunkgröße in Bytes (default: 64000)
                  --max-file-size <long>        Max. Dateigröße in Bytes (default: 5000000)

                Beispiele:
                  fastfilescrape tree --root . --include "**/*.java"
                  fastfilescrape content --root . --include "**/*.java" --out repo.txt
                  fastfilescrape all --root . --include "**/*.java" --format jsonl --out repo.jsonl
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

    private static void writeTreeJson(FastFileTree.Node node, Appendable out) throws IOException {
        out.append("{");
        out.append("\"path\":\"").append(escapeJson(node.path.toString())).append("\",");
        out.append("\"dir\":").append(String.valueOf(node.isDirectory)).append(",");
        out.append("\"size\":").append(String.valueOf(node.sizeBytes)).append(",");
        out.append("\"children\":[");
        for (int i = 0; i < node.children.size(); i++) {
            if (i > 0) out.append(",");
            writeTreeJson(node.children.get(i), out);
        }
        out.append("]}");
    }
}
