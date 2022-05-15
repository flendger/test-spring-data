package ru.flendger.testspringdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flendger.testspringdata.model.IncomingMessage;
import ru.flendger.testspringdata.model.MessageStatus;

import java.util.Optional;

public interface IncomingMessageRepository extends JpaRepository<IncomingMessage, Long> {
    Optional<IncomingMessage> findFirstByStatusOrderByIdAsc(MessageStatus status);
}