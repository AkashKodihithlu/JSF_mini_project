package com.relief.dao;

import com.relief.model.Allocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

@Repository
public class AllocationDAO {

    private final JdbcTemplate jdbcTemplate;

    public AllocationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BASE_SELECT = "SELECT a.*, " +
            "       p.donor_id, " +
            "       u_donor.name AS donor_name, " +
            "       u_donor.mobile_number AS donor_mobile, " +
            "       n.created_by AS affected_person_id, " +
            "       u_aff.name AS affected_person_name, " +
            "       n.category AS need_category, " +
            "       n.description AS need_description, " +
            "       n.urgency AS need_urgency, " +
            "       n.status AS need_status " +
            "FROM allocations a " +
            "LEFT JOIN pledges p ON a.pledge_id = p.pledge_id " +
            "LEFT JOIN users u_donor ON p.donor_id = u_donor.user_id " +
            "LEFT JOIN needs n ON a.need_id = n.need_id " +
            "LEFT JOIN users u_aff ON n.created_by = u_aff.user_id ";

    private final RowMapper<Allocation> allocationMapper = (rs, rowNum) -> {
        Allocation a = new Allocation();
        a.setAllocationId(rs.getInt("allocation_id"));
        a.setPledgeId(rs.getInt("pledge_id"));
        a.setNeedId(rs.getInt("need_id"));
        a.setQuantityAllocated(rs.getInt("quantity_allocated"));
        a.setStatus(rs.getString("status"));
        a.setAllocatedAt(rs.getTimestamp("allocated_at"));

        a.setCancellationReason(getStringSafe(rs, "cancellation_reason"));
        a.setCancelledAt(getTimestampSafe(rs, "cancelled_at"));
        a.setConfirmedAt(getTimestampSafe(rs, "confirmed_at"));

        a.setDonorId(getIntSafe(rs, "donor_id"));
        a.setDonorName(getStringSafe(rs, "donor_name"));
        a.setDonorMobile(getStringSafe(rs, "donor_mobile"));
        a.setAffectedPersonId(getIntSafe(rs, "affected_person_id"));
        a.setAffectedPersonName(getStringSafe(rs, "affected_person_name"));
        a.setNeedCategory(getStringSafe(rs, "need_category"));
        a.setNeedDescription(getStringSafe(rs, "need_description"));
        a.setNeedUrgency(getStringSafe(rs, "need_urgency"));
        a.setNeedStatus(getStringSafe(rs, "need_status"));
        return a;
    };

    public int save(Allocation allocation) {
        String status = (allocation.getStatus() == null || allocation.getStatus().isBlank())
                ? "ALLOCATED"
                : allocation.getStatus().trim().toUpperCase(Locale.ROOT);

        return jdbcTemplate.update(
                "INSERT INTO allocations (pledge_id, need_id, quantity_allocated, status) VALUES (?, ?, ?, ?)",
                allocation.getPledgeId(),
                allocation.getNeedId(),
                allocation.getQuantityAllocated(),
                status);
    }

    public List<Allocation> findAll() {
        return jdbcTemplate.query(
                BASE_SELECT + "ORDER BY a.allocated_at DESC, a.allocation_id DESC",
                allocationMapper);
    }

    public Allocation findById(int allocationId) {
        List<Allocation> list = jdbcTemplate.query(
                BASE_SELECT + "WHERE a.allocation_id = ?",
                allocationMapper,
                allocationId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Allocation> findByNeedId(int needId) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE a.need_id = ? ORDER BY a.allocated_at DESC, a.allocation_id DESC",
                allocationMapper,
                needId);
    }

    public List<Allocation> findByPledgeId(int pledgeId) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE a.pledge_id = ? ORDER BY a.allocated_at DESC, a.allocation_id DESC",
                allocationMapper,
                pledgeId);
    }

    public List<Allocation> findByDonorId(int donorId) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE p.donor_id = ? ORDER BY a.allocated_at DESC, a.allocation_id DESC",
                allocationMapper,
                donorId);
    }

    public List<Allocation> findByAffectedUserId(int affectedUserId) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE n.created_by = ? ORDER BY a.allocated_at DESC, a.allocation_id DESC",
                allocationMapper,
                affectedUserId);
    }

    public int updateStatus(int allocationId, String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("CONFIRMED".equals(normalized)) {
            return confirmReceipt(allocationId);
        }
        return jdbcTemplate.update(
                "UPDATE allocations SET status = ? WHERE allocation_id = ?",
                normalized,
                allocationId);
    }

    public int confirmReceipt(int allocationId) {
        return jdbcTemplate.update(
                "UPDATE allocations SET status = 'CONFIRMED', confirmed_at = CURRENT_TIMESTAMP WHERE allocation_id = ?",
                allocationId);
    }

    public int cancelWithReason(int allocationId, String reason) {
        return jdbcTemplate.update(
                "UPDATE allocations SET status = 'CANCELLED', cancellation_reason = ?, cancelled_at = CURRENT_TIMESTAMP "
                        +
                        "WHERE allocation_id = ?",
                reason.trim(),
                allocationId);
    }

    private static String getStringSafe(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException ignored) {
            return null;
        }
    }

    private static int getIntSafe(ResultSet rs, String col) {
        try {
            return rs.getInt(col);
        } catch (SQLException ignored) {
            return 0;
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