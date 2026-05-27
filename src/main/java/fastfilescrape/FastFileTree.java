package fastfilescrape;

import java.io.IOException;
import java.nio.file.*;
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

        // 1. native traverse and filter via FastGLOB
        Set<String> allRelativePaths = new LinkedHashSet<>();
        for (String includeGlob : cfg.includeGlobs) {
            String[] matches = fastglob.FastGLOB.glob(cfg.root.toString(), includeGlob);
            if (matches != null) {
                allRelativePaths.addAll(Arrays.asList(matches));
            }
        }

        // 2. Setup exclude matchers
        FileSystem fs = FileSystems.getDefault();
        List<PathMatcher> excludeMatchers = new ArrayList<>();
        for (String excludeGlob : cfg.excludeGlobs) {
            excludeMatchers.add(fs.getPathMatcher("glob:" + excludeGlob));
        }

        Node rootNode = new Node(cfg.root, true, 0L);

        // 3. Build tree structure from flat matched files
        for (String relStr : allRelativePaths) {
            Path relPath = Paths.get(relStr);

            // Check exclusions
            boolean excluded = false;
            for (PathMatcher matcher : excludeMatchers) {
                if (matcher.matches(relPath)) {
                    excluded = true;
                    break;
                }
                String cleanExclude = matcher.toString().replace("glob:", "").replace("/**", "");
                if (relStr.startsWith(cleanExclude)) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) {
                continue;
            }

            Path currentPath = cfg.root;
            Node currentNode = rootNode;

            for (int i = 0; i < relPath.getNameCount(); i++) {
                Path part = relPath.getName(i);
                currentPath = currentPath.resolve(part);
                boolean isLast = (i == relPath.getNameCount() - 1);

                Node childNode = null;
                for (Node child : currentNode.children) {
                    if (child.path.getFileName().equals(part)) {
                        childNode = child;
                        break;
                    }
                }

                if (childNode == null) {
                    if (isLast) {
                        long size = 0;
                        try {
                            size = Files.size(currentPath);
                        } catch (IOException e) {
                            // File might have been deleted or inaccessible
                        }
                        childNode = new Node(currentPath, false, size);
                    } else {
                        childNode = new Node(currentPath, true, 0L);
                    }
                    currentNode.children.add(childNode);
                }
                currentNode = childNode;
            }
        }

        pruneEmptyDirs(rootNode);
        sortTree(rootNode);
        return rootNode;
    }

    private static boolean pruneEmptyDirs(Node node) {
        if (!node.isDirectory) {
            return false;
        }
        node.children.removeIf(FastFileTree::pruneEmptyDirs);
        return node.children.isEmpty();
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
