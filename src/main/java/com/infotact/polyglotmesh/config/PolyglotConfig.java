package com.infotact.polyglotmesh.config;

import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class PolyglotConfig {

    static {
        try {
            Path cacheDir = Paths.get("target", ".graalvm-cache").toAbsolutePath();
            Files.createDirectories(cacheDir);
            if (System.getProperty("polyglot.engine.resourcePath") == null) {
                System.setProperty("polyglot.engine.resourcePath", cacheDir.toString());
            }
            if (System.getProperty("polyglotimpl.AttachLibraryFailureAction") == null) {
                System.setProperty("polyglotimpl.AttachLibraryFailureAction", "warn");
            }
        } catch (Exception ignored) {
        }
    }

    @Bean(destroyMethod = "close")
    public Engine sharedPolyglotEngine() {
        return Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    @Bean
    public HostAccess sandboxHostAccess() {
        return HostAccess.newBuilder(HostAccess.EXPLICIT)
                .allowArrayAccess(true)
                .allowListAccess(true)
                .allowMapAccess(true)
                .build();
    }
}
