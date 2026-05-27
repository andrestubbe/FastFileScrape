package fastfilescrape;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class FastFileScrapeContent {

    public static final class Config {
        public Path root;
        public List<String> includeGlobs = List.of("**/*.java", "**/*.cpp");
        public List<String> excludeGlobs = List.of(".git/**", "build/**", "out/**");
        public int maxChunkBytes = 64_000;
        public long maxFileSizeBytes = 5_000_000;
        public Charset charset = StandardCharsets.UTF_8;
    }

    public interface Sink {
        void onChunk(Path file, int chunkIndex, CharSequence content) throws IOException;
    }

    public static void scrape(Config cfg, Sink sink) throws IOException {
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
        List<String> cleanExcludes = new ArrayList<>();
        List<String> fastExcludes = new ArrayList<>();
        for (String excludeGlob : cfg.excludeGlobs) {
            excludeMatchers.add(fs.getPathMatcher("glob:" + excludeGlob));
            cleanExcludes.add(excludeGlob.replace("glob:", "").replace("/**", ""));
            
            String clean = excludeGlob.replace("**/", "").replace("/**", "");
            if (!clean.isEmpty()) {
                fastExcludes.add(clean);
            }
        }

        // 3. Process matches natively
        for (String relStr : allRelativePaths) {
            Path relPath = Paths.get(relStr);

            // Check exclusions
            boolean excluded = false;
            
            // Fast-path simple string contains (orders of magnitude faster than regex PathMatcher)
            for (String fastEx : fastExcludes) {
                if (relStr.contains("/" + fastEx + "/") || relStr.startsWith(fastEx + "/")) {
                    excluded = true;
                    break;
                }
            }
            
            if (!excluded) {
                for (int k = 0; k < excludeMatchers.size(); k++) {
                    PathMatcher matcher = excludeMatchers.get(k);
                    if (matcher.matches(relPath)) {
                        excluded = true;
                        break;
                    }
                    String cleanExclude = cleanExcludes.get(k);
                    if (relStr.startsWith(cleanExclude)) {
                        excluded = true;
                        break;
                    }
                }
            }
            
            if (excluded) {
                continue;
            }

            Path absolutePath = cfg.root.resolve(relPath);
            try {
                long size = absolutePath.toFile().length();
                if (size > cfg.maxFileSizeBytes) {
                    continue;
                }
                String content = new String(Files.readAllBytes(absolutePath), cfg.charset);
                Chunker.chunk(content, cfg.maxChunkBytes, (chunkIndex, chunk) -> {
                    try {
                        sink.onChunk(absolutePath, chunkIndex, chunk);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                // Ignore missing or inaccessible files
            }
        }
    }
}
