package com.tzachi.file_observer.worker;

import com.tzachi.file_observer.config.FileObserverConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Data
@Slf4j
public class FileObserverWorker extends Thread {

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private Map<String, FileObserverConfiguration.FileObserverData> fileIngestionMap;
    private Set<String> fileIngestionDirectories;

    public FileObserverWorker(Map<String, FileObserverConfiguration.FileObserverData> fileIngestionMap,
                              Set<String> fileIngestionDirectories) {
        this.fileIngestionMap = fileIngestionMap;
        this.fileIngestionDirectories = fileIngestionDirectories;
    }

    public void run() {
        log.info("Start file observer worker");

        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            for (String fileIngestionDirectory : fileIngestionDirectories) {
                Path directory = Paths.get(fileIngestionDirectory);
                directory.register(watchService, ENTRY_CREATE);
            }
            WatchKey watchKey = null;
            while (true) {
                watchKey = watchService.poll(50, MILLISECONDS);
                if(watchKey != null) {
                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == ENTRY_CREATE) {
                            Path filePath = (Path) event.context();
                            String fileName = filePath.getFileName().toString();

                            log.debug("Got event on a new file : {}", filePath.getFileName());
                            fileIngestionMap.values()
                                    .stream()
                                    .filter(fileObserverData -> matchFileName(fileObserverData.getPattern(), fileName,fileObserverData.getObserverName()))
                                    .findFirst()
                                    .ifPresent(fileObserverData -> {
                                        String absolutePath = getAbsolutePath(fileName, fileObserverData.getRegisterDirectory());
                                        insertFileNameToQueue(absolutePath, fileObserverData);
                                    });
                        }
                    }
                    watchKey.reset();
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO TS : workaround - watch service return original file directory and not the register directory
    private String getAbsolutePath(String fileName, String registerPath) {
        return registerPath + FILE_SEPARATOR + fileName;
    }

    private static void insertFileNameToQueue(String fileName, FileObserverConfiguration.FileObserverData fileObserverData) {
        try {
            log.info("[File observer name: {}] Put file producing job on internal queue.  file name: {} ",
                    fileObserverData.getObserverName(), fileName);
            fileObserverData.getQueue().put(fileName);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean matchFileName(Pattern pattern, String fileName, String observerName) {
        boolean matches = pattern.matcher(fileName).matches();
        log.info("[File observer name: {}] " +
                "Pattern match result: {} for file name: {} , pattern: {}",observerName,  matches, fileName, pattern);
        return matches;
    }
}
