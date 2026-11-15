package com.relief.dao;

import com.relief.model.Need;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NeedDAO {

    private final JdbcTemplate jdbcTemplate;

    public NeedDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Need> needMapper = (rs, rowNum) -> {
        Need n = new Need();
        n.setNeedId(rs.getInt("need_id"));
        n.setDisasterId(rs.getInt("disaster_id"));
        n.setCreatedBy(rs.getInt("created_by"));
        n.setCategory(rs.getString("category"));
        n.setDescription(rs.getString("description"));
        n.setQuantityRequired(rs.getInt("quantity_required"));
        n.setQuantityPledged(rs.getInt("quantity_pledged"));
        n.setQuantityAllocated(rs.getInt("quantity_allocated"));
        n.setQuantityRemaining(rs.getInt("quantity_remaining"));
        n.setUrgency(rs.getString("urgency"));
        n.setStatus(rs.getString("status"));
        n.setLatitude(rs.getDouble("latitude"));
        n.setLongitude(rs.getDouble("longitude"));
        return n;
    };

    public int save(Need need) {
        String urgency = (need.getUrgency() == null || need.getUrgency().isBlank())
                ? "MEDIUM"
                : need.getUrgency().trim().toUpperCase();
        String status = (need.getStatus() == null || need.getStatus().isBlank())
                ? "PENDING_VERIFICATION"
                : need.getStatus().trim().toUpperCase();

        return jdbcTemplate.update(
                "INSERT INTO needs (disaster_id, created_by, category, description, quantity_required, " +
                        "quantity_pledged, quantity_allocated, quantity_remaining, urgency, status, latitude, longitude) "
                        +
                        "VALUES (?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?, ?)",
                need.getDisasterId(),
                need.getCreatedBy(),
                need.getCategory().trim(),
                need.getDescription().trim(),
                need.getQuantityRequired(),
                need.getQuantityRequired(),
                urgency,
                status,
                need.getLatitude(),
                need.getLongitude());
    }

    public List<Need> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM needs ORDER BY FIELD(urgency, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'), need_id DESC",
                needMapper);
    }

    public Need findById(int needId) {
        List<Need> list = jdbcTemplate.query(
                "SELECT * FROM needs WHERE need_id = ?",
                needMapper,
                needId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Need> findByCreatedBy(int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM needs WHERE created_by = ? ORDER BY need_id DESC",
                needMapper,
                userId);
    }

    public List<Need> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM needs WHERE status = ? ORDER BY FIELD(urgency, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'), need_id DESC",
                needMapper,
                status.trim().toUpperCase());
    }

    public List<Need> findAvailableForDonors() {
        return jdbcTemplate.query(
                "SELECT * FROM needs WHERE status IN ('VERIFIED', 'PARTIALLY_FULFILLED') AND quantity_remaining > 0 " +
                        "ORDER BY FIELD(urgency, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'), need_id DESC",
                needMapper);
    }

    public List<String> findDescriptionsByDisaster(int disasterId) {
        return jdbcTemplate.queryForList(
                "SELECT description FROM needs WHERE disaster_id = ? AND status != 'REJECTED'",
                String.class,
                disasterId);
    }

    public int updateStatusAndUrgency(int needId, String status, String urgency) {
        return jdbcTemplate.update(
                "UPDATE needs SET status = ?, urgency = ? WHERE need_id = ?",
                status.trim().toUpperCase(),
                urgency.trim().toUpperCase(),
                needId);
    }

    public int updateQuantitiesAndStatus(int needId, int quantityPledged, int quantityAllocated,
            int quantityRemaining, String status) {
        return jdbcTemplate.update(
                "UPDATE needs SET quantity_pledged = ?, quantity_allocated = ?, quantity_remaining = ?, status = ? " +
                        "WHERE need_id = ?",
                quantityPledged,
                quantityAllocated,
                quantityRemaining,
                status.trim().toUpperCase(),
                needId);
    }
}