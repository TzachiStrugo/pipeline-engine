package com.tzachi.file_observer;


import com.tzachi.file_observer.config.FileObserverProperties;
import com.tzachi.file_observer.worker.FileObserverWorker;
import com.tzachi.file_observer.worker.FileProducerWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@SpringBootConfiguration
@ComponentScan(basePackages = "com.tzachi.file_observer")
@EnableConfigurationProperties(FileObserverProperties.class)
@Slf4j
@AllArgsConstructor
public class FileObserverApplication implements CommandLineRunner {

    private final List<FileProducerWorker> fileProducerWorkers;
    private final FileObserverWorker fileObserverWorker;

    public static void main(String[] args) {
        log.info("starting File Ingestion Application");
        SpringApplication.run(FileObserverApplication.class, args);
    }

    @Override
    public void run(String... args) {
        fileProducerWorkers.forEach(Thread::start);
        fileObserverWorker.start();
    }
}
