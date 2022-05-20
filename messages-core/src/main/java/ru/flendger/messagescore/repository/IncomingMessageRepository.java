package ru.flendger.messagescore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flendger.messagescore.model.IncomingMessage;
import ru.flendger.messagescore.model.MessageStatus;

import java.util.Optional;

public interface IncomingMessageRepository extends JpaRepository<IncomingMessage, Long> {
    Optional<IncomingMessage> findFirstByStatusOrderByIdAsc(MessageStatus status);
}