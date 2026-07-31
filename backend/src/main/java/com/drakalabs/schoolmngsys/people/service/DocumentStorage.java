package com.drakalabs.schoolmngsys.people.service;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;

/**
 * Where uploaded documents live — filesystem or object storage, both behind access-checked
 * streaming (docs/11 §4). Decided at WP-3 (docs/14 §8): filesystem for now
 * ({@link FilesystemDocumentStorage}), swappable to object storage later without touching callers.
 */
public interface DocumentStorage {

    String store(String originalName, InputStream content) throws IOException;

    Resource load(String storageKey) throws IOException;
}
