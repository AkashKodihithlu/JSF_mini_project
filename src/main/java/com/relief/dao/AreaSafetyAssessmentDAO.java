package com.relief.dao;

import com.relief.model.AreaSafetyAssessment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class AreaSafetyAssessmentDAO {

    private final JdbcTemplate jdbcTemplate;

    public AreaSafetyAssessmentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AreaSafetyAssessment> mapper = (rs, rowNum) -> {
        AreaSafetyAssessment a = new AreaSafetyAssessment();
        a.setAssessmentId(rs.getInt("assessment_id"));
        a.setDisasterId(rs.getInt("disaster_id"));
        a.setDepartmentId(rs.getInt("department_id"));
        a.setAreaName(rs.getString("area_name"));
        a.setLatitude(rs.getDouble("latitude"));
        a.setLongitude(rs.getDouble("longitude"));
        a.setRadiusKm(rs.getDouble("radius_km"));
        a.setSafetyStatus(rs.getString("safety_status"));
        a.setFloodLevelMeters(rs.getObject("flood_level_meters") != null ? rs.getDouble("flood_level_meters") : null);
        a.setAssessmentNotes(rs.getString("assessment_notes"));
        a.setAssessedAt(rs.getTimestamp("assessed_at"));
        try {
            a.setDepartmentOfficerName(rs.getString("officer_name"));
        } catch (Exception ignored) {}
        return a;
    };

    /**
     * Persists a new area safety assessment record (immutable audit trail).
     */
    public int save(AreaSafetyAssessment assessment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO area_safety_assessments " +
                "(disaster_id, department_id, area_name, latitude, longitude, radius_km, safety_status, flood_level_meters, assessment_notes, assessed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, assessment.getDisasterId());
            ps.setInt(2, assessment.getDepartmentId());
            ps.setString(3, assessment.getAreaName().trim());
            ps.setDouble(4, assessment.getLatitude());
            ps.setDouble(5, assessment.getLongitude());
            ps.setDouble(6, assessment.getRadiusKm());
            ps.setString(7, assessment.getSafetyStatus().trim().toUpperCase());
            if (assessment.getFloodLevelMeters() != null) {
                ps.setDouble(8, assessment.getFloodLevelMeters());
            } else {
                ps.setNull(8, java.sql.Types.DECIMAL);
            }
            ps.setString(9, assessment.getAssessmentNotes() != null ? assessment.getAssessmentNotes().trim() : null);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            assessment.setAssessmentId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public AreaSafetyAssessment findById(int assessmentId) {
        List<AreaSafetyAssessment> list = jdbcTemplate.query(
                "SELECT a.*, u.name AS officer_name FROM area_safety_assessments a " +
                        "LEFT JOIN users u ON a.department_id = u.user_id " +
                        "WHERE a.assessment_id = ?",
                mapper,
                assessmentId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Returns the latest safety assessment for each distinct area under a disaster event.
     * Uses composite geographical identity (area_name + rounded coordinates) to prevent
     * distinct geographical locations sharing the same name from being improperly merged.
     */
    public List<AreaSafetyAssessment> findLatestByDisaster(int disasterId) {
        String sql = "SELECT a.*, u.name AS officer_name FROM area_safety_assessments a " +
                "LEFT JOIN users u ON a.department_id = u.user_id " +
                "WHERE a.assessment_id IN (" +
                "    SELECT MAX(assessment_id) FROM area_safety_assessments " +
                "    WHERE disaster_id = ? " +
                "    GROUP BY area_name, ROUND(latitude, 3), ROUND(longitude, 3)" +
                ") ORDER BY a.area_name ASC, a.assessment_id DESC";
        return jdbcTemplate.query(sql, mapper, disasterId);
    }

    /**
     * Returns the complete chronological assessment history for a specific area, preserving historical assessments.
     */
    public List<AreaSafetyAssessment> findHistoryByDisasterAndArea(int disasterId, String areaName) {
        String sql = "SELECT a.*, u.name AS officer_name FROM area_safety_assessments a " +
                "LEFT JOIN users u ON a.department_id = u.user_id " +
                "WHERE a.disaster_id = ? AND a.area_name = ? " +
                "ORDER BY a.assessed_at DESC, a.assessment_id DESC";
        return jdbcTemplate.query(sql, mapper, disasterId, areaName.trim());
    }

    /**
     * Returns chronological assessment history for a specific area and geographic coordinate.
     */
    public List<AreaSafetyAssessment> findHistoryByDisasterAndLocation(int disasterId, String areaName, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return findHistoryByDisasterAndArea(disasterId, areaName);
        }
        String sql = "SELECT a.*, u.name AS officer_name FROM area_safety_assessments a " +
                "LEFT JOIN users u ON a.department_id = u.user_id " +
                "WHERE a.disaster_id = ? AND a.area_name = ? " +
                "AND ROUND(a.latitude, 3) = ROUND(?, 3) AND ROUND(a.longitude, 3) = ROUND(?, 3) " +
                "ORDER BY a.assessed_at DESC, a.assessment_id DESC";
        return jdbcTemplate.query(sql, mapper, disasterId, areaName.trim(), latitude, longitude);
    }

    /**
     * Returns all historical safety assessments for a disaster.
     */
    public List<AreaSafetyAssessment> findAllByDisaster(int disasterId) {
        String sql = "SELECT a.*, u.name AS officer_name FROM area_safety_assessments a " +
                "LEFT JOIN users u ON a.department_id = u.user_id " +
                "WHERE a.disaster_id = ? " +
                "ORDER BY a.assessed_at DESC, a.assessment_id DESC";
        return jdbcTemplate.query(sql, mapper, disasterId);
    }
}
