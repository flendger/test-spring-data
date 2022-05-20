package ru.flendger.messagescore.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.flendger.messagescore.model.IncomingMessage;
import ru.flendger.messagescore.model.MessageStatus;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
class IncomingMessageUpdateServiceTest {
    @Autowired
    private IncomingMessageService incomingMessageService;

    @Autowired
    private IncomingMessageHandler handler;

    private final List<IncomingMessage> sourceMessages = new ArrayList<>();

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
                    log.error(e.getMessage());
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