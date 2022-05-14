package ru.flendger.testspringdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flendger.testspringdata.model.IncomingMessage;
import ru.flendger.testspringdata.repository.IncomingMessageRepository;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class IncomingMessageService {
    private final IncomingMessageRepository incomingMessageRepository;

    public IncomingMessage findById(Long id) {
        return incomingMessageRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public IncomingMessage save(IncomingMessage incomingMessage) {
        return incomingMessageRepository.saveAndFlush(incomingMessage);
    }

    @Transactional
    public IncomingMessage saveAndPause(IncomingMessage incomingMessage, long pause) {
        IncomingMessage message = incomingMessageRepository.saveAndFlush(incomingMessage);

        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    public void delete(IncomingMessage incomingMessage) {
        incomingMessageRepository.delete(incomingMessage);
    }
}
