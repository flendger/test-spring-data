package ru.flendger.testspringdata.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.flendger.testspringdata.model.IncomingMessage;
import ru.flendger.testspringdata.model.MessageStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
class IncomingMessageInsertServiceTest {
    @Autowired
    private IncomingMessageService incomingMessageService;

    private final List<IncomingMessage> sourceMessages = new ArrayList<>();

    @Test
    public void insertTest() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.submit(() -> {
            IncomingMessage message = generateMessage("uuid_1");
            log.info("start save uuid_1 with pause");

            try {
                sourceMessages.add(incomingMessageService.saveAndPause(message, 1000));
                log.info("end save uuid_1 with pause");
            } catch (Exception e) {
                log.error("save uuid_1 with pause FAILED");
            }
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        executorService.submit(() -> {
            IncomingMessage message = generateMessage("uuid_1");
            log.info("start save uuid_1 without pause");

            try {
                sourceMessages.add(incomingMessageService.save(message));
                log.info("end save uuid_1 without pause");
            } catch (Exception e) {
                log.error("save uuid_1 without pause FAILED");
            }
        });

        executorService.submit(() -> {
            IncomingMessage message = generateMessage("uuid_2");
            log.info("start save uuid_2");
            sourceMessages.add(incomingMessageService.save(message));
            log.info("end save uuid_2");
        });

        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(2, sourceMessages.size());
        assertTrue(sourceMessages.stream().anyMatch(incomingMessage -> Objects.equals("uuid_1", incomingMessage.getExternalId())));
        assertTrue(sourceMessages.stream().anyMatch(incomingMessage -> Objects.equals("uuid_2", incomingMessage.getExternalId())));
    }

    @AfterEach
    void tearDown() {
        sourceMessages.forEach(incomingMessageService::delete);
    }

    private IncomingMessage generateMessage(String externalId) {
        IncomingMessage message = new IncomingMessage();
        message.setExternalId(externalId);
        message.setStatus(MessageStatus.PENDING);

        return message;
    }
}