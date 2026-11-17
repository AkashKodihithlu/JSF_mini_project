package com.relief.dao;

import com.relief.model.Pledge;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

@Repository
public class PledgeDAO {

    private final JdbcTemplate jdbcTemplate;

    public PledgeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BASE_SELECT = "SELECT p.*, " +
            "       u.name AS donor_name, " +
            "       n.category AS need_category, " +
            "       n.description AS need_description, " +
            "       n.urgency AS need_urgency, " +
            "       n.status AS need_status " +
            "FROM pledges p " +
            "LEFT JOIN users u ON p.donor_id = u.user_id " +
            "LEFT JOIN needs n ON p.need_id = n.need_id ";

    private final RowMapper<Pledge> pledgeMapper = (rs, rowNum) -> {
        Pledge p = new Pledge();
        p.setPledgeId(rs.getInt("pledge_id"));
        p.setNeedId(rs.getInt("need_id"));
        p.setDonorId(rs.getInt("donor_id"));
        p.setQuantityOffered(rs.getInt("quantity_offered"));
        p.setQuantityAllocated(rs.getInt("quantity_allocated"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));

        p.setCancellationReason(getStringSafe(rs, "cancellation_reason"));
        p.setCancelledAt(getTimestampSafe(rs, "cancelled_at"));
        p.setDonorName(getStringSafe(rs, "donor_name"));
        p.setNeedCategory(getStringSafe(rs, "need_category"));
        p.setNeedDescription(getStringSafe(rs, "need_description"));
        p.setNeedUrgency(getStringSafe(rs, "need_urgency"));
        p.setNeedStatus(getStringSafe(rs, "need_status"));
        return p;
    };

    public int save(Pledge pledge) {
        String status = (pledge.getStatus() == null || pledge.getStatus().isBlank())
                ? "PLEDGED"
                : pledge.getStatus().trim().toUpperCase(Locale.ROOT);

        return jdbcTemplate.update(
                "INSERT INTO pledges (need_id, donor_id, quantity_offered, quantity_allocated, status) " +
                        "VALUES (?, ?, ?, ?, ?)",
                pledge.getNeedId(),
                pledge.getDonorId(),
                pledge.getQuantityOffered(),
                pledge.getQuantityAllocated(),
                status);
    }

    public List<Pledge> findAll() {
        return jdbcTemplate.query(
                BASE_SELECT + "ORDER BY p.created_at DESC, p.pledge_id DESC",
                pledgeMapper);
    }

    public Pledge findById(int pledgeId) {
        List<Pledge> list = jdbcTemplate.query(
                BASE_SELECT + "WHERE p.pledge_id = ?",
                pledgeMapper,
                pledgeId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Pledge> findByDonorId(int donorId) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE p.donor_id = ? ORDER BY p.created_at DESC, p.pledge_id DESC",
                pledgeMapper,
                donorId);
    }

    public List<Pledge> findByNeedId(int needId) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE p.need_id = ? ORDER BY p.created_at DESC, p.pledge_id DESC",
                pledgeMapper,
                needId);
    }

    public List<Pledge> findSurplusPledges() {
        return jdbcTemplate.query(
                BASE_SELECT +
                        "WHERE p.quantity_offered > p.quantity_allocated " +
                        "AND p.status IN ('PLEDGED', 'PARTIALLY_ALLOCATED') " +
                        "ORDER BY p.created_at ASC, p.pledge_id ASC",
                pledgeMapper);
    }

    public int updateAllocationAndStatus(int pledgeId, int quantityAllocated, String status) {
        return jdbcTemplate.update(
                "UPDATE pledges SET quantity_allocated = ?, status = ? WHERE pledge_id = ?",
                quantityAllocated,
                status.trim().toUpperCase(Locale.ROOT),
                pledgeId);
    }

    public int updateStatus(int pledgeId, String status) {
        return jdbcTemplate.update(
                "UPDATE pledges SET status = ? WHERE pledge_id = ?",
                status.trim().toUpperCase(Locale.ROOT),
                pledgeId);
    }

    public int cancelWithReason(int pledgeId, String reason) {
        return jdbcTemplate.update(
                "UPDATE pledges SET status = 'CANCELLED', cancellation_reason = ?, cancelled_at = CURRENT_TIMESTAMP " +
                        "WHERE pledge_id = ?",
                reason.trim(),
                pledgeId);
    }

    private static String getStringSafe(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException ignored) {
            return null;
        }
    }

    private static Timestamp getTimestampSafe(ResultSet rs, String col) {
        try {
            return rs.getTimestamp(col);
        } catch (SQLException ignored) {
            return null;
        }
    }
}