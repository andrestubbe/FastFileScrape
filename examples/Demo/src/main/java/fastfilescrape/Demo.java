package fastfilescrape;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Premium Hero Demo for FastFileScrape natively accelerated by FastGLOB.
 */
public class Demo {
    public static void main(String[] args) throws Exception {
        System.out.println("=== FastFileScrape Natively Accelerated Demo ===");
        
        // 1. Build and print the directory tree of FastFileScrape project
        Path root = Paths.get(".").toAbsolutePath().normalize();
        System.out.println("Building tree for: " + root);
        
        FastFileTree.Config tcfg = new FastFileTree.Config();
        tcfg.root = root;
        tcfg.includeGlobs = List.of("src/**/*.java", "pom.xml");
        tcfg.excludeGlobs = List.of("**/target/**");
        
        FastFileTree.Node tree = FastFileTree.build(tcfg);
        
        System.out.println("\n--- NATIVE DIRECTORY TREE (Source Files Only) ---");
        FastFileTree.printTree(tree, System.out);
        
        // 2. Scrape and chunk Java file contents
        System.out.println("\n--- NATIVE FILE CONTENT SCRAPER (First Chunks) ---");
        FastFileScrapeContent.Config ccfg = new FastFileScrapeContent.Config();
        ccfg.root = root;
        ccfg.includeGlobs = List.of("src/main/java/fastfilescrape/Chunker.java");
        ccfg.maxChunkBytes = 1000; // Small chunks for clean console output
        
        FastFileScrapeContent.scrape(ccfg, (file, chunkIndex, content) -> {
            System.out.printf("📄 File: %s (Chunk #%d)\n", root.relativize(file), chunkIndex);
            System.out.println("----------------------------------------------------------------");
            System.out.println(content);
            System.out.println("----------------------------------------------------------------\n");
        });
        
        System.out.println("=== Demo Complete ===");
    }
}
