package com.relief.dao;

import com.relief.model.Pledge;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PledgeDAO {

    private final JdbcTemplate jdbcTemplate;

    public PledgeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Pledge> pledgeMapper = (rs, rowNum) -> {
        Pledge p = new Pledge();
        p.setPledgeId(rs.getInt("pledge_id"));
        p.setNeedId(rs.getInt("need_id"));
        p.setDonorId(rs.getInt("donor_id"));
        p.setQuantityOffered(rs.getInt("quantity_offered"));
        p.setQuantityAllocated(rs.getInt("quantity_allocated"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    };

    public int save(Pledge pledge) {
        String status = (pledge.getStatus() == null || pledge.getStatus().isBlank())
                ? "PLEDGED"
                : pledge.getStatus().trim().toUpperCase();

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
                "SELECT * FROM pledges ORDER BY created_at DESC, pledge_id DESC",
                pledgeMapper);
    }

    public Pledge findById(int pledgeId) {
        List<Pledge> list = jdbcTemplate.query(
                "SELECT * FROM pledges WHERE pledge_id = ?",
                pledgeMapper,
                pledgeId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Pledge> findByDonorId(int donorId) {
        return jdbcTemplate.query(
                "SELECT * FROM pledges WHERE donor_id = ? ORDER BY created_at DESC, pledge_id DESC",
                pledgeMapper,
                donorId);
    }

    public List<Pledge> findByNeedId(int needId) {
        return jdbcTemplate.query(
                "SELECT * FROM pledges WHERE need_id = ? ORDER BY created_at DESC, pledge_id DESC",
                pledgeMapper,
                needId);
    }

    public List<Pledge> findSurplusPledges() {
        return jdbcTemplate.query(
                "SELECT * FROM pledges WHERE quantity_offered > quantity_allocated " +
                        "AND status IN ('PLEDGED', 'PARTIALLY_ALLOCATED') " +
                        "ORDER BY created_at ASC, pledge_id ASC",
                pledgeMapper);
    }

    public int updateAllocationAndStatus(int pledgeId, int quantityAllocated, String status) {
        return jdbcTemplate.update(
                "UPDATE pledges SET quantity_allocated = ?, status = ? WHERE pledge_id = ?",
                quantityAllocated,
                status.trim().toUpperCase(),
                pledgeId);
    }

    public int updateStatus(int pledgeId, String status) {
        return jdbcTemplate.update(
                "UPDATE pledges SET status = ? WHERE pledge_id = ?",
                status.trim().toUpperCase(),
                pledgeId);
    }
}