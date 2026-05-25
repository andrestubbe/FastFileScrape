package fastfilescrape;

import java.nio.file.*;
import java.util.*;

final class GlobMatcher {

    private final List<PathMatcher> includes;
    private final List<PathMatcher> excludes;
    private final Path root;

    GlobMatcher(List<String> includeGlobs, List<String> excludeGlobs, Path root) {
        FileSystem fs = FileSystems.getDefault();
        this.includes = new ArrayList<>();
        this.excludes = new ArrayList<>();
        for (String g : includeGlobs) {
            includes.add(fs.getPathMatcher("glob:" + g));
        }
        for (String g : excludeGlobs) {
            excludes.add(fs.getPathMatcher("glob:" + g));
        }
        this.root = root.toAbsolutePath().normalize();
    }

    private Path rel(Path p) {
        Path abs = p.toAbsolutePath().normalize();
        return root.relativize(abs);
    }

    boolean matchesDir(Path dir) {
        Path r = rel(dir);

        // If excluded → skip whole subtree
        if (isExcluded(r)) return false;

        // Directories are always allowed unless excluded
        return true;
    }


    boolean matchesFile(Path file) {
        Path r = rel(file);
        if (isExcluded(r)) return false;
        if (includes.isEmpty()) return true;
        for (PathMatcher m : includes) {
            if (m.matches(r)) return true;
        }
        return false;
    }

    private boolean isExcluded(Path r) {
        for (PathMatcher m : excludes) {
            if (m.matches(r)) return true;

            // also check prefix match for directory patterns like ".git/**"
            if (r.toString().startsWith(m.toString().replace("glob:", "").replace("/**", ""))) {
                return true;
            }
        }
        return false;
    }

}
