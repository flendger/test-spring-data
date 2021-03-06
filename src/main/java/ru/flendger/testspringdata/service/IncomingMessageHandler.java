package ru.flendger.testspringdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.flendger.testspringdata.model.IncomingMessage;

@Service
@RequiredArgsConstructor
public class IncomingMessageHandler {
    private final IncomingMessageService incomingMessageService;

    public IncomingMessage handleNext() {
        IncomingMessage message = incomingMessageService.progressNext();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return incomingMessageService.complete(message);
    }
}
