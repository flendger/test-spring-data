package ru.flendger.testspringdata.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.flendger.testspringdata.model.IncomingMessage;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class IncomingMessageServiceTest {
    @Autowired
    private IncomingMessageService incomingMessageService;

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

    private IncomingMessage generateMessage(String externalId) {
        IncomingMessage message = new IncomingMessage();
        message.setExternalId(externalId);

        return message;
    }
}