package com.interviai.backend.config;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads KEY=VALUE pairs from a project {@code .env} into JVM system properties
 * when those keys are not already present in the process environment.
 * <p>
 * Lets {@code mvn spring-boot:run} / IDE launches pick up root {@code .env}
 * without requiring the shell scripts to export variables first.
 */
public final class DotenvLoader {

    private DotenvLoader() {}

    public static void load() {
        Path envFile = locateEnvFile();
        if (envFile == null) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
            String line;
            int loaded = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (key.isEmpty() || !key.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    continue;
                }
                // Skip blanks so empty .env placeholders don't override Spring defaults
                if (value.isBlank()) {
                    continue;
                }
                // Prefer real process env; only fill gaps for local .env convenience
                if (System.getenv(key) != null && !System.getenv(key).isBlank()) {
                    continue;
                }
                if (System.getProperty(key) != null && !System.getProperty(key).isBlank()) {
                    continue;
                }
                System.setProperty(key, value);
                loaded++;
            }
            if (loaded > 0) {
                System.out.println("[dotenv] Loaded " + loaded + " vars from " + envFile.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[dotenv] Failed to read " + envFile + ": " + e.getMessage());
        }
    }

    private static Path locateEnvFile() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = List.of(
                cwd.resolve(".env"),
                cwd.resolve("..").resolve(".env").normalize(),
                cwd.getParent() != null ? cwd.getParent().resolve(".env") : null
        );
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
