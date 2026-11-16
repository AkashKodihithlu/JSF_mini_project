package com.relief.dao;

import com.relief.model.Message;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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
        m.setContent(rs.getString("content"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        m.setSenderName(rs.getString("sender_name"));
        return m;
    };

    public int save(Message message) {
        return jdbcTemplate.update(
                "INSERT INTO messages (need_id, sender_id, receiver_id, content) VALUES (?, ?, ?, ?)",
                message.getNeedId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent().trim());
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