package com.relief.dao;

import com.relief.model.Allocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AllocationDAO {

    private final JdbcTemplate jdbcTemplate;

    public AllocationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Allocation> allocationMapper = (rs, rowNum) -> {
        Allocation a = new Allocation();
        a.setAllocationId(rs.getInt("allocation_id"));
        a.setPledgeId(rs.getInt("pledge_id"));
        a.setNeedId(rs.getInt("need_id"));
        a.setQuantityAllocated(rs.getInt("quantity_allocated"));
        a.setStatus(rs.getString("status"));
        a.setAllocatedAt(rs.getTimestamp("allocated_at"));
        return a;
    };

    public int save(Allocation allocation) {
        String status = (allocation.getStatus() == null || allocation.getStatus().isBlank())
                ? "ALLOCATED"
                : allocation.getStatus().trim().toUpperCase();

        return jdbcTemplate.update(
                "INSERT INTO allocations (pledge_id, need_id, quantity_allocated, status) VALUES (?, ?, ?, ?)",
                allocation.getPledgeId(),
                allocation.getNeedId(),
                allocation.getQuantityAllocated(),
                status);
    }

    public List<Allocation> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM allocations ORDER BY allocated_at DESC, allocation_id DESC",
                allocationMapper);
    }

    public Allocation findById(int allocationId) {
        List<Allocation> list = jdbcTemplate.query(
                "SELECT * FROM allocations WHERE allocation_id = ?",
                allocationMapper,
                allocationId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Allocation> findByNeedId(int needId) {
        return jdbcTemplate.query(
                "SELECT * FROM allocations WHERE need_id = ? ORDER BY allocated_at DESC, allocation_id DESC",
                allocationMapper,
                needId);
    }

    public List<Allocation> findByPledgeId(int pledgeId) {
        return jdbcTemplate.query(
                "SELECT * FROM allocations WHERE pledge_id = ? ORDER BY allocated_at DESC, allocation_id DESC",
                allocationMapper,
                pledgeId);
    }

    public int updateStatus(int allocationId, String status) {
        return jdbcTemplate.update(
                "UPDATE allocations SET status = ? WHERE allocation_id = ?",
                status.trim().toUpperCase(),
                allocationId);
    }
}