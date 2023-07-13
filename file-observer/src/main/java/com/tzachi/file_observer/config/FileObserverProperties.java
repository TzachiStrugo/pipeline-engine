package com.tzachi.file_observer.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "com.tzachi.data-engine")
@ConfigurationPropertiesScan
@Data
@Slf4j
public class FileObserverProperties {

    private String brokers;

    private Map<String, FileObserver> fileObserver = new LinkedHashMap<>();

    @Data
    public static class FileObserver {
        private String filePattern;
        private FileStorage storage;
        private Processing processing;
    }

    @Data
    public static class FileStorage {
        private String type;
        private Map<String, String> config;
    }

    @Data
    public static class Processing {
        private String type;
        private Topic produceToTopic;
        private Map<String, String> config = new LinkedHashMap<>();
    }

    @Data
    public static class Topic {
        private String name;
        private int partitions;
        private short replicationFactor;
    }

}
