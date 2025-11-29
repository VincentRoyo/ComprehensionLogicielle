package org.example;

import org.example.processors.EndpointLoggerProcessor;
import spoon.Launcher;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.support.StandardEnvironment;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        String in  = getEnvOrDefault("IN_SRC",  "/app/src/main/java/com/example/tp3restructuring/controller");
        String out = getEnvOrDefault("OUT_DIR", "/app/src-instrumented");

        System.out.println("===== [SPOON DEBUG] =====");
        System.out.println("IN_SRC  = " + in);
        System.out.println("OUT_DIR = " + out);

        requireDirExists(in, "❌ Input sources not found");
        ensureDir(out);

        // --- 1. Liste rapide des fichiers source
        try (Stream<Path> s = Files.walk(Path.of(in))) {
            List<Path> javaFiles = s.filter(f -> f.toString().endsWith(".java")).toList();
            System.out.println("📁 Found " + javaFiles.size() + " Java files under " + in);
            javaFiles.stream().limit(10).forEach(p -> System.out.println("   - " + p));
        } catch (IOException e) {
            System.err.println("⚠️ Cannot walk input dir: " + e.getMessage());
        }

        // --- 2. Config Spoon
        Launcher launcher = new Launcher();
        StandardEnvironment env = (StandardEnvironment) launcher.getEnvironment();
        env.setNoClasspath(true);
        env.setAutoImports(true);
        env.setCommentEnabled(true);
        env.setDebug(true);
        env.setVerbose(true);
        env.setIgnoreSyntaxErrors(true);

        launcher.addInputResource(in);
        launcher.setSourceOutputDirectory(out);


        Processor<CtMethod<?>> p = new EndpointLoggerProcessor();
        launcher.addProcessor(p);

        // --- 3. Build model
        System.out.println("⏳ Building Spoon model...");
        CtModel model = launcher.buildModel();
        List<CtClass<?>> classes = model.getElements(e -> e instanceof CtClass)
                .stream().map(e -> (CtClass<?>) e).collect(Collectors.toList());
        System.out.println("📦 Found " + classes.size() + " classes in total.");

        int totalMethods = 0;
        for (CtClass<?> c : classes) {
            int count = c.getMethods().size();
            totalMethods += count;
            System.out.println("   • " + c.getQualifiedName() + " : " + count + " methods");
        }
        System.out.println("🔢 Total methods: " + totalMethods);

        // --- 4. Run processor
        System.out.println("⚙️ Running EndpointLoggerProcessor...");
        launcher.process();

        // --- 5. Write transformed sources
        System.out.println("💾 Writing instrumented sources to " + out);
        launcher.prettyprint();

        System.out.println("✅ [SPOON] Instrumentation done. Output → " + out);
        System.out.println("===========================");
    }

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static void requireDirExists(String p, String msg) {
        if (!Files.isDirectory(Path.of(p))) {
            System.err.println(msg + ": " + p);
            System.exit(2);
        }
    }

    private static void ensureDir(String p) {
        try {
            Files.createDirectories(Path.of(p));
        } catch (Exception e) {
            System.err.println("Cannot create output dir: " + p);
            System.exit(3);
        }
    }
}
