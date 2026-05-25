package fastfilescrape;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
        GlobMatcher matcher = new GlobMatcher(cfg.includeGlobs, cfg.excludeGlobs, cfg.root);

        Files.walkFileTree(cfg.root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!matcher.matchesDir(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!matcher.matchesFile(file)) {
                    return FileVisitResult.CONTINUE;
                }
                if (attrs.size() > cfg.maxFileSizeBytes) {
                    return FileVisitResult.CONTINUE;
                }
                String content = Files.readString(file, cfg.charset);
                Chunker.chunk(content, cfg.maxChunkBytes, (chunkIndex, chunk) -> {
                    try {
                        sink.onChunk(file, chunkIndex, chunk);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
