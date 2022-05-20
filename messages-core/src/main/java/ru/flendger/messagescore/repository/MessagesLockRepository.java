package ru.flendger.messagescore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessagesLockRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void lock(String locker) {
        SqlParameterSource parameters = new MapSqlParameterSource().addValue("locker", locker);

        try {
            namedParameterJdbcTemplate.queryForObject(
                    "select locker from messages_lock where locker = :locker for update",
                    parameters,
                    String.class);
        } catch (DataAccessException e) {
            namedParameterJdbcTemplate.update("insert into messages_lock values (:locker)", parameters);
        }
    }
}
