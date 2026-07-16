package fastfilescrape;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public final class FastFileTree {

    public static final class Node {
        public final Path path;
        public final boolean isDirectory;
        public final long sizeBytes;
        public final List<Node> children = new ArrayList<>();

        public Node(Path path, boolean isDirectory, long sizeBytes) {
            this.path = path;
            this.isDirectory = isDirectory;
            this.sizeBytes = sizeBytes;
        }
    }

    public static final class Config {
        public Path root;
        public List<String> includeGlobs = List.of("**/*");
        public List<String> excludeGlobs = List.of(".git/**", "build/**", "out/**");
    }

    public static Node build(Config cfg) throws IOException {
        Objects.requireNonNull(cfg.root, "root");
        GlobMatcher matcher = new GlobMatcher(cfg.includeGlobs, cfg.excludeGlobs, cfg.root);

        Node rootNode = new Node(cfg.root, true, 0L);
        Map<Path, Node> map = new HashMap<>();
        map.put(cfg.root.toAbsolutePath().normalize(), rootNode);

        Files.walkFileTree(cfg.root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!matcher.matchesDir(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (!dir.equals(cfg.root)) {
                    Node parent = map.get(dir.getParent().toAbsolutePath().normalize());
                    if (parent != null) {
                        Node node = new Node(dir, true, 0L);
                        parent.children.add(node);
                        map.put(dir.toAbsolutePath().normalize(), node);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!matcher.matchesFile(file)) {
                    return FileVisitResult.CONTINUE;
                }
                Node parent = map.get(file.getParent().toAbsolutePath().normalize());
                if (parent != null) {
                    Node node = new Node(file, false, attrs.size());
                    parent.children.add(node);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        sortTree(rootNode);
        return rootNode;
    }

    private static void sortTree(Node node) {
        node.children.sort((a, b) -> {
            if (a.isDirectory && !b.isDirectory) return -1;
            if (!a.isDirectory && b.isDirectory) return 1;
            return a.path.getFileName().toString().compareToIgnoreCase(b.path.getFileName().toString());
        });
        for (Node c : node.children) {
            if (c.isDirectory) sortTree(c);
        }
    }

    public static void printTree(Node node, Appendable out) throws IOException {
        printTree(node, out, "", true, true);
    }

    private static void printTree(Node node, Appendable out, String prefix, boolean isLast, boolean isRoot) throws IOException {
        if (!isRoot) {
            out.append(prefix);
            out.append(isLast ? "└── " : "├── ");
            out.append(node.path.getFileName().toString());
            if (!node.isDirectory) {
                out.append(" (").append(String.valueOf(node.sizeBytes)).append(" bytes)");
            }
            out.append('\n');
            prefix += isLast ? "    " : "│   ";
        }
        for (int i = 0; i < node.children.size(); i++) {
            Node c = node.children.get(i);
            printTree(c, out, prefix, i == node.children.size() - 1, false);
        }
    }
}
