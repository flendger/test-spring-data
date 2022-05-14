package ru.flendger.testspringdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}
