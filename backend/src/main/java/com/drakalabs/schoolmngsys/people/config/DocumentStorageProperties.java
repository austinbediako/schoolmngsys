package com.drakalabs.schoolmngsys.people.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record DocumentStorageProperties(String documentsDir) {
}
