package ru.flendger.testspringdata.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.flendger.testspringdata.model.IncomingMessage;
import ru.flendger.testspringdata.model.MessageStatus;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class IncomingMessageServiceTest {
    @Autowired
    private IncomingMessageService incomingMessageService;

    @Autowired
    private IncomingMessageHandler handler;

    private final List<IncomingMessage> sourceMessages = new ArrayList<>();

    @Test
    public void insertTest() {
        Queue<IncomingMessage> messages = new ConcurrentLinkedQueue<>();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.submit(() -> {
            IncomingMessage message = generateMessage("uuid_1");
            log.info("start save uuid_1 with pause");

            try {
                messages.add(incomingMessageService.saveAndPause(message, 1000));
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
                messages.add(incomingMessageService.save(message));
                log.info("end save uuid_1 without pause");
            } catch (Exception e) {
                log.error("save uuid_1 without pause FAILED");
            }
        });

        executorService.submit(() -> {
            IncomingMessage message = generateMessage("uuid_2");
            log.info("start save uuid_2");
            messages.add(incomingMessageService.save(message));
            log.info("end save uuid_2");
        });

        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(2, messages.size());
        assertTrue(messages.stream().anyMatch(incomingMessage -> Objects.equals("uuid_1", incomingMessage.getExternalId())));
        assertTrue(messages.stream().anyMatch(incomingMessage -> Objects.equals("uuid_2", incomingMessage.getExternalId())));

        log.info("DELETING MESSAGES");
        messages.forEach(incomingMessageService::delete);
    }

    @Test
    public void updateTest() {
        for (int i = 0; i < 100; i++) {
            sourceMessages.add(incomingMessageService.save(generateMessage("uuid_" + 100 + i)));
        }

        Queue<IncomingMessage> handledMessages = new ConcurrentLinkedQueue<>();

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            executorService.submit(getProgressNext(handledMessages));
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(100, handledMessages.size());

        Map<String, List<IncomingMessage>> messagesByUuid =
                handledMessages
                        .stream()
                        .collect(Collectors.groupingBy(IncomingMessage::getExternalId));

        messagesByUuid.forEach((uuid, incomingMessages) -> assertEquals(1, incomingMessages.size()));
    }

    @AfterEach
    void tearDown() {
        sourceMessages.forEach(incomingMessageService::delete);
    }

    private Runnable getProgressNext(Queue<IncomingMessage> handledMessages) {
        return () -> {
            while (true) {
                try {
                    IncomingMessage message = handler.handleNext();
                    handledMessages.add(message);
                } catch (Exception e) {
                    break;
                }
            }
        };
    }

    private IncomingMessage generateMessage(String externalId) {
        IncomingMessage message = new IncomingMessage();
        message.setExternalId(externalId);
        message.setStatus(MessageStatus.PENDING);

        return message;
    }
}