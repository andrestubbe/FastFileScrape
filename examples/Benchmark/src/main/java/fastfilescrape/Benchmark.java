package fastfilescrape;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance Benchmark comparing FastFileScrape (natively accelerated by FastGLOB)
 * against standard Java NIO directory traversal and file matching.
 */
public class Benchmark {
    public static void main(String[] args) throws Exception {
        System.out.println("=== FastFileScrape Performance Benchmark ===");
        
        // Go up to the workspace root to scrape a large number of projects
        Path searchRoot = Paths.get("..").toAbsolutePath().normalize();
        List<String> includeGlobs = List.of("**/*.java");
        List<String> excludeGlobs = List.of("**/target/**", "**/.git/**", "**/.idea/**");
        
        System.out.println("Scraping root: " + searchRoot);
        
        // Warmup
        for (int i = 0; i < 3; i++) {
            runJavaScraper(searchRoot, includeGlobs, excludeGlobs);
            runFastFileScraper(searchRoot, includeGlobs, excludeGlobs);
        }
        
        int runs = 5;
        System.out.println("\nRunning " + runs + " iterations...");
        
        // 1. Benchmark Standard Java
        long startJava = System.nanoTime();
        int javaChunks = 0;
        for (int i = 0; i < runs; i++) {
            javaChunks = runJavaScraper(searchRoot, includeGlobs, excludeGlobs);
        }
        long javaTimeMs = (System.nanoTime() - startJava) / 1_000_000 / runs;
        
        // 2. Benchmark FastFileScrape (Native)
        long startNative = System.nanoTime();
        int nativeChunks = 0;
        for (int i = 0; i < runs; i++) {
            nativeChunks = runFastFileScraper(searchRoot, includeGlobs, excludeGlobs);
        }
        long nativeTimeMs = (System.nanoTime() - startNative) / 1_000_000 / runs;
        
        System.out.println("\n=== Results ===");
        System.out.println("Standard Java Scraped Chunks: " + javaChunks + " in " + javaTimeMs + " ms (avg)");
        System.out.println("FastFileScrape Scraped Chunks: " + nativeChunks + " in " + nativeTimeMs + " ms (avg)");
        
        if (nativeTimeMs > 0) {
            float speedup = (float) javaTimeMs / nativeTimeMs;
            System.out.printf("Speedup:                     %.2fx faster! 🚀\n", speedup);
        } else {
            System.out.println("Speedup:                     Infinite (Native was too fast to measure) 🚀");
        }
    }
    
    private static int runJavaScraper(Path root, List<String> includeGlobs, List<String> excludeGlobs) throws IOException {
        FileSystem fs = FileSystems.getDefault();
        List<PathMatcher> includes = new ArrayList<>();
        List<PathMatcher> excludes = new ArrayList<>();
        for (String g : includeGlobs) includes.add(fs.getPathMatcher("glob:" + g));
        for (String g : excludeGlobs) excludes.add(fs.getPathMatcher("glob:" + g));
        
        final int[] chunkCount = {0};
        
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path r = root.relativize(dir);
                for (PathMatcher m : excludes) {
                    if (m.matches(r) || r.toString().startsWith(m.toString().replace("glob:", "").replace("/**", ""))) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path r = root.relativize(file);
                for (PathMatcher m : excludes) {
                    if (m.matches(r)) return FileVisitResult.CONTINUE;
                }
                boolean matched = false;
                for (PathMatcher m : includes) {
                    if (m.matches(r)) {
                        matched = true;
                        break;
                    }
                }
                if (matched && attrs.size() <= 5_000_000) {
                    String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                    // Mock chunking
                    Chunker.chunk(content, 64000, (chunkIndex, chunk) -> {
                        chunkCount[0]++;
                    });
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return chunkCount[0];
    }
    
    private static int runFastFileScraper(Path root, List<String> includeGlobs, List<String> excludeGlobs) throws IOException {
        long t0 = System.nanoTime();
        FastFileScrapeContent.Config cfg = new FastFileScrapeContent.Config();
        cfg.root = root;
        cfg.includeGlobs = includeGlobs;
        cfg.excludeGlobs = excludeGlobs;
        cfg.maxChunkBytes = 64000;
        cfg.maxFileSizeBytes = 5_000_000;
        
        final AtomicInteger chunkCount = new AtomicInteger(0);
        
        long tStart = System.nanoTime();
        // Step 1: Native glob JNI call
        Set<String> allRelativePaths = new LinkedHashSet<>();
        for (String includeGlob : cfg.includeGlobs) {
            String[] matches = fastglob.FastGLOB.glob(cfg.root.toString(), includeGlob);
            if (matches != null) {
                allRelativePaths.addAll(Arrays.asList(matches));
            }
        }
        long tJni = System.nanoTime() - tStart;
        
        long tFilterStart = System.nanoTime();
        // Setup exclude matchers
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
        long tFilter = System.nanoTime() - tFilterStart;
        
        long tReadStart = System.nanoTime();
        allRelativePaths.parallelStream().forEach(relStr -> {
            Path relPath = Paths.get(relStr);
            
            // Check exclusions
            boolean excluded = false;
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
            
            if (!excluded) {
                Path absolutePath = cfg.root.resolve(relPath);
                try {
                    long size = absolutePath.toFile().length();
                    if (size <= cfg.maxFileSizeBytes) {
                        String content = new String(Files.readAllBytes(absolutePath), cfg.charset);
                        Chunker.chunk(content, cfg.maxChunkBytes, (chunkIndex, chunk) -> {
                            chunkCount.incrementAndGet();
                        });
                    }
                } catch (IOException e) {
                    // Ignore missing or inaccessible files
                }
            }
        });
        long tRead = System.nanoTime() - tReadStart;
        
        System.out.printf("  [Telemetry] JNI: %.2f ms | Filter Setup: %.2f ms | Read/Chunk: %.2f ms | Total: %.2f ms\n",
            tJni / 1_000_000.0, tFilter / 1_000_000.0, tRead / 1_000_000.0, (System.nanoTime() - t0) / 1_000_000.0);
            
        return chunkCount.get();
    }
}
