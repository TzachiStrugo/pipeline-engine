package com.tzachi.file_observer.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.BlockingQueue;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@AllArgsConstructor
public class FileProducerWorker extends Thread {

    private final BlockingQueue<String> fileNameIngestionQueue;
    private final KafkaTemplate<String, String> producer;
    private final String fileObserverName;

    //TODO TS - Optimization: consider to use batch size and max wait time instead to do it one by one
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        log.info("[File observer name: {}] Start kafka producer worker", fileObserverName);
        String filePath;
        try {
            while (true) {
                filePath = fileNameIngestionQueue.take();
                log.info("[File observer name: {}] Send event file created to topic: {} path: {}]",
                        fileObserverName, producer.getDefaultTopic(), filePath);
                // Send event partition by timestamp
                producer.send(producer.getDefaultTopic(), filePath, filePath);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
