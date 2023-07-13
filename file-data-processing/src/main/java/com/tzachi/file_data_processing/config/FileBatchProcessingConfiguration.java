package com.tzachi.file_data_processing.config;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
@AllArgsConstructor
@EnableKafka
@Slf4j
public class FileBatchProcessingConfiguration {

    private final FileDataProcessingProperties fileDataProcessingProperties;

    private DefaultListableBeanFactory autowireCapableBeanFactory;

    @Bean
    public SparkSession sparkSession() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("Spark file batch processing")
                .setMaster("local[*]");

        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put(BOOTSTRAP_SERVERS_CONFIG, fileDataProcessingProperties.getBrokers());
        kafkaConfig.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConfig.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConfig.put(GROUP_ID_CONFIG, "file-data-processing");

        kafkaConfig.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaConsumerFactory<>(kafkaConfig);
//        kafkaListenerContainerFactory(consumerFactory);

    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory ) {
//        fileDataProcessingProperties.getFileDataProcessing().values().stream().map((key, value) -> {
//            ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                    new ConcurrentKafkaListenerContainerFactory<>();
//            factory.setConsumerFactory(kafkaConsumerFactory);
//            factory.setConcurrency(value.getConsumeFromTopic().getConcurrency());
//            autowireCapableBeanFactory.registerSingleton("kafka-factory-" + key, factory);
//            return factory;
//        });


        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setConcurrency(1);
//        autowireCapableBeanFactory.registerSingleton("kafka-factory-" + key, factory);
        return factory;
    }
//
//    @KafkaListener(
//            topics = "fct.vehicle_status_file_name.timestamp",
//            containerFactory = KAFKA_FACTORY_PREFIX + VEHICLE_STATUS_DATA_PROCESSING_KEY)
//    public void processFileNames(String fileNameEvent) {
//        log.info("consume file name : {} ", fileNameEvent);
//        FileDataProcessingProperties.DataProcessing dataProcessing =
//                fileDataProcessingProperties.getFileDataProcessing().get(VEHICLE_STATUS_DATA_PROCESSING_KEY);
//
//        Dataset<Row> dataFrame = createDataFrame(fileNameEvent, dataProcessing);
//        dataFrame.write()
//                .format(CASSANDRA_FORMAT_WRITER)
//                .options(ImmutableMap.of(
//                        "keyspace", VEHICLES_KEYSPACE,
//                        "table", "vehicle_status"))
//                .mode(SaveMode.Append)
//                .save();
//    }
}
