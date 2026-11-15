package com.relief.dao;

import com.relief.model.Alert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlertDAO {

    private final JdbcTemplate jdbcTemplate;

    public AlertDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Alert> alertMapper = (rs, rowNum) -> {
        Alert a = new Alert();
        a.setAlertId(rs.getInt("alert_id"));
        a.setDisasterId(rs.getInt("disaster_id"));
        a.setCreatedBy(rs.getInt("created_by"));
        a.setTitle(rs.getString("title"));
        a.setMessage(rs.getString("message"));
        a.setLatitude(rs.getDouble("latitude"));
        a.setLongitude(rs.getDouble("longitude"));
        a.setRadiusKm(rs.getDouble("radius_km"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    };

    public int save(Alert alert) {
        return jdbcTemplate.update(
                "INSERT INTO alerts (disaster_id, created_by, title, message, latitude, longitude, radius_km) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                alert.getDisasterId(),
                alert.getCreatedBy(),
                alert.getTitle().trim(),
                alert.getMessage().trim(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getRadiusKm());
    }

    public List<Alert> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM alerts ORDER BY created_at DESC, alert_id DESC",
                alertMapper);
    }
}