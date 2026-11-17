package com.relief.dao;

import com.relief.model.NeedParticipant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class NeedParticipantDAO {

    private final JdbcTemplate jdbcTemplate;

    public NeedParticipantDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<NeedParticipant> mapper = (rs, rowNum) -> {
        NeedParticipant np = new NeedParticipant();
        np.setParticipantId(rs.getInt("participant_id"));
        np.setNeedId(rs.getInt("need_id"));
        np.setUserId(rs.getInt("user_id"));
        np.setJoinedAt(rs.getTimestamp("joined_at"));
        np.setLeftAt(rs.getTimestamp("left_at"));
        np.setParticipationStatus(rs.getString("participation_status"));
        return np;
    };

    public List<NeedParticipant> findByNeedAndUser(int needId, int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM need_participants WHERE need_id = ? AND user_id = ? ORDER BY joined_at ASC",
                mapper,
                needId,
                userId);
    }

    public List<NeedParticipant> findActiveByNeed(int needId) {
        return jdbcTemplate.query(
                "SELECT * FROM need_participants WHERE need_id = ? AND participation_status = 'ACTIVE' ORDER BY joined_at ASC",
                mapper,
                needId);
    }

    public void addOrActivateParticipant(int needId, int userId) {
        List<NeedParticipant> existing = jdbcTemplate.query(
                "SELECT * FROM need_participants WHERE need_id = ? AND user_id = ? AND participation_status = 'ACTIVE'",
                mapper,
                needId,
                userId);
        if (existing.isEmpty()) {
            jdbcTemplate.update(
                    "INSERT INTO need_participants (need_id, user_id, joined_at, left_at, participation_status) " +
                            "VALUES (?, ?, CURRENT_TIMESTAMP, NULL, 'ACTIVE')",
                    needId,
                    userId);
        }
    }

    public void withdrawParticipant(int needId, int userId) {
        jdbcTemplate.update(
                "UPDATE need_participants SET left_at = CURRENT_TIMESTAMP, participation_status = 'WITHDRAWN' " +
                        "WHERE need_id = ? AND user_id = ? AND participation_status = 'ACTIVE'",
                needId,
                userId);
    }

    /**
     * Checks if a user was an active participant of a need at a specific point in time.
     */
    public boolean isUserParticipantAt(int needId, int userId, Timestamp timestamp) {
        List<NeedParticipant> list = findByNeedAndUser(needId, userId);
        for (NeedParticipant np : list) {
            if (np.isActiveDuring(timestamp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a user has any active participation record for a need.
     */
    public boolean hasActiveParticipation(int needId, int userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM need_participants WHERE need_id = ? AND user_id = ? AND participation_status = 'ACTIVE'",
                Integer.class,
                needId,
                userId);
        return count != null && count > 0;
    }

    /**
     * Checks if a user has ever participated in this need (historical or active).
     */
    public boolean hasAnyParticipation(int needId, int userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM need_participants WHERE need_id = ? AND user_id = ?",
                Integer.class,
                needId,
                userId);
        return count != null && count > 0;
    }
}
