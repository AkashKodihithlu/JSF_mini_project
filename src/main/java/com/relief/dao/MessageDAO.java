package com.relief.dao;

import com.relief.model.Message;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class MessageDAO {

    private final JdbcTemplate jdbcTemplate;

    public MessageDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Message> messageMapper = (rs, rowNum) -> {
        Message m = new Message();
        m.setMessageId(rs.getInt("message_id"));
        m.setNeedId(rs.getInt("need_id"));
        m.setSenderId(rs.getInt("sender_id"));
        int recId = rs.getInt("receiver_id");
        m.setReceiverId(rs.wasNull() ? null : recId);

        String text = null;
        try {
            text = rs.getString("content");
        } catch (SQLException ignored) {}
        if (text == null) {
            try {
                text = rs.getString("message");
            } catch (SQLException ignored) {}
        }
        m.setContent(text != null ? text : "");
        m.setSentAt(rs.getTimestamp("sent_at"));

        try {
            m.setSenderName(rs.getString("sender_name"));
        } catch (SQLException ignored) {
            m.setSenderName("User");
        }
        return m;
    };

    public int save(Message message) {
        String content = message.getContent() != null ? message.getContent().trim() : "";
        try {
            return jdbcTemplate.update(
                    "INSERT INTO messages (need_id, sender_id, receiver_id, content) VALUES (?, ?, ?, ?)",
                    message.getNeedId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    content);
        } catch (DataAccessException e) {
            // Fallback if table column is named 'message'
            return jdbcTemplate.update(
                    "INSERT INTO messages (need_id, sender_id, receiver_id, message) VALUES (?, ?, ?, ?)",
                    message.getNeedId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    content);
        }
    }

    public List<Message> findByNeedId(int needId) {
        return jdbcTemplate.query(
                "SELECT m.*, COALESCE(u.name, 'User') AS sender_name " +
                        "FROM messages m LEFT JOIN users u ON m.sender_id = u.user_id " +
                        "WHERE m.need_id = ? ORDER BY m.sent_at ASC, m.message_id ASC",
                messageMapper,
                needId);
    }
}
