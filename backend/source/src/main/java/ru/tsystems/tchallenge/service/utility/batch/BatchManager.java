package ru.tsystems.tchallenge.service.utility.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class BatchManager {

    private ExecutorService executorService;

    public void submit(BatchRunnable runnable) {
        this.executorService.submit(() -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                handleBatchException(exception);
            }
        });
    }

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(3);
    }

    private void handleBatchException(Exception exception) {
        log.error("Batch operation failure", exception);
    }
}
