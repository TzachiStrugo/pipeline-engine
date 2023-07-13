package com.tzachi.file_data_processing;


import com.google.common.collect.ImmutableMap;
import com.tzachi.file_data_processing.config.FileDataProcessingProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootConfiguration
@ComponentScan(basePackages = "com.tzachi.file_data_processing")
@EnableConfigurationProperties(FileDataProcessingProperties.class)
@Slf4j
@AllArgsConstructor
public class FileBatchProcessingApplication implements CommandLineRunner {

    private static final String CASSANDRA_FORMAT_WRITER = "org.apache.spark.sql.cassandra";
    private static final String VEHICLES_KEYSPACE = "vehicles_keyspace";
    private static final  String GROUP_ID = "file-data-processing";
    private static final String VEHICLE_STATUS_DATA_PROCESSING_KEY = "vehicle-status";

    private static final String KAFKA_FACTORY_PREFIX = "kafka-factory-";

    private final SparkSession sparkSession;

    private final FileDataProcessingProperties fileDataProcessingProperties;

    public static void main(String[] args) {
        log.info("starting file batch processing application");
        SpringApplication.run(FileBatchProcessingApplication.class, args);
    }

    @Override
    public void run(String... args) {
            while (true) {
            try {
                Thread.sleep(1000); // Sleep for 1 second
            } catch (InterruptedException e) {
                log.error("Error occurred while sleeping", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @KafkaListener(
            topics = "fct.vehicle_status_file_name.timestamp",
            groupId = GROUP_ID)
//            containerFactory = KAFKA_FACTORY_PREFIX + VEHICLE_STATUS_DATA_PROCESSING_KEY)
    public void processFileNames(String fileNameEvent) {
        log.info("consume file name : {} ", fileNameEvent);
        FileDataProcessingProperties.DataProcessing dataProcessing =
                fileDataProcessingProperties.getFileDataProcessing().get(VEHICLE_STATUS_DATA_PROCESSING_KEY);

        Dataset<Row> dataFrame = createDataFrame(fileNameEvent, dataProcessing);
        dataFrame.write()
                .format(CASSANDRA_FORMAT_WRITER)
                .options(ImmutableMap.of(
                        "keyspace", VEHICLES_KEYSPACE,
                        "table", "vehicle_status"))
                .mode(SaveMode.Append)
                .save();
    }

    private Dataset<Row> createDataFrame(String fileNameEvent, FileDataProcessingProperties.DataProcessing dataProcessing) {
        Dataset<Row> jsonData = sparkSession.read().json(fileNameEvent);

        JavaRDD<Row> repartitionedData = jsonData.javaRDD().repartition(dataProcessing.getProcessing().getRepartition());
        return sparkSession.createDataFrame(repartitionedData, jsonData.schema());
    }
}
