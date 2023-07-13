package com.tzachi.file_data_processing.config;

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
public class FileDataProcessingProperties {

    private String brokers;
    private  Map<String, DataProcessing> fileDataProcessing = new LinkedHashMap<>();

    @Data
    public static class DataProcessing {

        private Processing processing;
        private Topic consumeFromTopic;
    }

    @Data
    public static class Processing {
        private int repartition;
    }

    @Data
    public static class Topic {
        private String name;
        private int concurrency;
    }
}
