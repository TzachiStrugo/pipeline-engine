package com.tzachi.file_observer.config;


import com.tzachi.file_observer.worker.FileObserverWorker;
import com.tzachi.file_observer.worker.FileProducerWorker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
@AllArgsConstructor
@Slf4j
public class FileObserverConfiguration {

    private final FileObserverProperties fileObserverProperties;

    private DefaultListableBeanFactory autowireCapableBeanFactory;

    @Bean
    public FileObserverWorker fileIngestionWorker(Map<String, FileObserverData> fileObserverNameToDataMap) {
        Set<String> allFileIngestionDirectories = fileObserverProperties.getFileObserver()
                .values()
                .stream()
                .map(s -> s.getStorage().getConfig().get("directory"))
                .collect(Collectors.toSet());
        return new FileObserverWorker(fileObserverNameToDataMap, allFileIngestionDirectories);
    }

    @Bean
    public List<FileProducerWorker> fileProducerWorkers(KafkaAdmin kafkaAdmin,
                                                        Map<String, FileObserverData> fileObserverNameToDataMap) {
        autoProvisionTopic(kafkaAdmin);
        return fileObserverNameToDataMap.entrySet()
                .stream()
                .map(entry -> new FileProducerWorker(entry.getValue().getQueue(),
                        entry.getValue().getProducer(), entry.getKey()))
                .collect(Collectors.toList());
    }

    private void autoProvisionTopic(KafkaAdmin kafkaAdmin) {
        fileObserverProperties.getFileObserver()
                .values()
                .forEach(fileObserver -> {
                    FileObserverProperties.Topic topicProperties = fileObserver.getProcessing().getProduceToTopic();

                    log.info("init kafka topic : {} ",topicProperties.getName());
                    NewTopic newTopic = new NewTopic(
                            topicProperties.getName(),
                            topicProperties.getPartitions(),
                            topicProperties.getReplicationFactor());
                    autowireCapableBeanFactory.registerSingleton("topic:" + topicProperties.getName(), newTopic);
                    kafkaAdmin.initialize();
                });
    }

    @Bean
    public Map<String, FileObserverData> fileObserverNameToDataMap(DefaultKafkaProducerFactory<String, String> producerFactory) {
        return fileObserverProperties.getFileObserver().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> buildFileObserverData(producerFactory, entry)
                ));
    }

    @NotNull
    private FileObserverData buildFileObserverData(DefaultKafkaProducerFactory<String, String> producerFactory, Map.Entry<String, FileObserverProperties.FileObserver> entry) {
        FileObserverProperties.Topic topic = entry.getValue().getProcessing().getProduceToTopic();
        KafkaTemplate<String, String> kafkaTemplate = createKafkaTemplate(topic, producerFactory);
        Pattern pattern = Pattern.compile(entry.getValue().getFilePattern());
        //TODO TS: move blocking queue size to configuration file
        return FileObserverData.builder()
                .pattern(pattern)
                .observerName(entry.getKey())
                .registerDirectory(entry.getValue().getStorage().getConfig().get("directory"))
                .queue(new ArrayBlockingQueue<>(1000))
                .producer(kafkaTemplate)
                .build();
    }

    KafkaTemplate<String, String> createKafkaTemplate(FileObserverProperties.Topic topic, DefaultKafkaProducerFactory<String, String> producerFactory) {
        log.info("create kafka template for topic : {} ", topic.getName());
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory, true);
        kafkaTemplate.setDefaultTopic(topic.getName());
        return kafkaTemplate;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, fileObserverProperties.getBrokers());
        return new KafkaAdmin(configs);
    }

    @Bean
    DefaultKafkaProducerFactory<String, String> producerFactory() {
        log.info("init DefaultKafkaProducerFactory");
        Map<String, Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put(BOOTSTRAP_SERVERS_CONFIG, fileObserverProperties.getBrokers());
        kafkaConfig.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(kafkaConfig);
    }

    @Builder
    @Getter
    public static class FileObserverData {
        private Pattern pattern;
        private BlockingQueue<String> queue;
        private KafkaTemplate<String, String> producer;
        private String observerName;
        private String registerDirectory;
    }

}
