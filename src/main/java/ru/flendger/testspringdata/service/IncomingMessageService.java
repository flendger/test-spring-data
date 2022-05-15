package ru.flendger.testspringdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flendger.testspringdata.model.IncomingMessage;
import ru.flendger.testspringdata.model.MessageStatus;
import ru.flendger.testspringdata.repository.IncomingMessageRepository;
import ru.flendger.testspringdata.repository.MessagesLockRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncomingMessageService {
    private final IncomingMessageRepository incomingMessageRepository;
    private final MessagesLockRepository lockRepository;

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

    @Transactional
    public IncomingMessage progressNext() throws EntityNotFoundException {
        lockRepository.lock("lock");

        Optional<IncomingMessage> messageOptional = incomingMessageRepository.findFirstByStatusOrderByIdAsc(MessageStatus.PENDING);
        if (messageOptional.isEmpty()) {
            throw new EntityNotFoundException("No messages for progress");
        }

        IncomingMessage message = messageOptional.get();
        message.setStatus(MessageStatus.PROGRESS);
        return incomingMessageRepository.saveAndFlush(message);
    }

    @Transactional
    public IncomingMessage complete(IncomingMessage incomingMessage) {
        incomingMessage.setStatus(MessageStatus.COMPLETE);
        return incomingMessageRepository.saveAndFlush(incomingMessage);
    }

    public void delete(IncomingMessage incomingMessage) {
        incomingMessageRepository.delete(incomingMessage);
    }
}
