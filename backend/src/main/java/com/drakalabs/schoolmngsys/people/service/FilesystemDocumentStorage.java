package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.config.DocumentStorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(DocumentStorageProperties.class)
public class FilesystemDocumentStorage implements DocumentStorage {

    private final Path rootDir;

    public FilesystemDocumentStorage(DocumentStorageProperties properties) {
        this.rootDir = Path.of(properties.documentsDir());
    }

    @Override
    public String store(String originalName, InputStream content) throws IOException {
        Files.createDirectories(rootDir);
        String storageKey = UUID.randomUUID() + "-" + sanitize(originalName);
        Path target = rootDir.resolve(storageKey);
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        return storageKey;
    }

    @Override
    public Resource load(String storageKey) {
        return new PathResource(rootDir.resolve(storageKey));
    }

    private String sanitize(String originalName) {
        return originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
