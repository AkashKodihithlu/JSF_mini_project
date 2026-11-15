package com.relief.dao;

import com.relief.model.Disaster;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DisasterDAO {

    private final JdbcTemplate jdbcTemplate;

    public DisasterDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Disaster> disasterMapper = (rs, rowNum) -> {
        Disaster d = new Disaster();
        d.setDisasterId(rs.getInt("disaster_id"));
        d.setName(rs.getString("name"));
        d.setType(rs.getString("type"));
        d.setDescription(rs.getString("description"));
        d.setStartDate(rs.getDate("start_date"));
        d.setEndDate(rs.getDate("end_date"));
        d.setStatus(rs.getString("status"));
        return d;
    };

    public List<Disaster> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM disasters ORDER BY start_date DESC, disaster_id DESC",
                disasterMapper);
    }

    public List<Disaster> findActiveDisasters() {
        return jdbcTemplate.query(
                "SELECT * FROM disasters WHERE status = 'ACTIVE' ORDER BY start_date DESC, disaster_id DESC",
                disasterMapper);
    }

    public Disaster findById(int disasterId) {
        List<Disaster> list = jdbcTemplate.query(
                "SELECT * FROM disasters WHERE disaster_id = ?",
                disasterMapper,
                disasterId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int save(Disaster disaster) {
        String status = (disaster.getStatus() == null || disaster.getStatus().isBlank())
                ? "ACTIVE"
                : disaster.getStatus().trim().toUpperCase();

        return jdbcTemplate.update(
                "INSERT INTO disasters (name, type, description, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)",
                disaster.getName(),
                disaster.getType(),
                disaster.getDescription(),
                disaster.getStartDate(),
                disaster.getEndDate(),
                status);
    }

    public int updateStatus(int disasterId, String status) {
        return jdbcTemplate.update(
                "UPDATE disasters SET status = ? WHERE disaster_id = ?",
                status.trim().toUpperCase(),
                disasterId);
    }
}