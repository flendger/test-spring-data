package ru.flendger.testspringdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flendger.testspringdata.model.IncomingMessage;

public interface IncomingMessageRepository extends JpaRepository<IncomingMessage, Long> {
}