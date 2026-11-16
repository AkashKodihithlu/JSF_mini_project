package com.relief.dao;

import com.relief.model.NeedVerification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NeedVerificationDAO {

    private final JdbcTemplate jdbcTemplate;

    public NeedVerificationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<NeedVerification> verificationMapper = (rs, rowNum) -> {
        NeedVerification nv = new NeedVerification();
        nv.setVerificationId(rs.getInt("verification_id"));
        nv.setNeedId(rs.getInt("need_id"));
        nv.setDepartmentId(rs.getInt("department_id"));
        nv.setStatus(rs.getString("status"));
        nv.setRemarks(rs.getString("remarks"));
        nv.setVerifiedAt(rs.getTimestamp("verified_at"));
        return nv;
    };

    public int save(NeedVerification verification) {
        String status = verification.getStatus() == null
                ? "APPROVED"
                : verification.getStatus().trim().toUpperCase();
        String remarks = verification.getRemarks() == null
                ? ""
                : verification.getRemarks().trim();

        return jdbcTemplate.update(
                "INSERT INTO need_verification (need_id, department_id, status, remarks) VALUES (?, ?, ?, ?)",
                verification.getNeedId(),
                verification.getDepartmentId(),
                status,
                remarks);
    }

    public List<NeedVerification> findByNeedId(int needId) {
        return jdbcTemplate.query(
                "SELECT * FROM need_verification WHERE need_id = ? ORDER BY verified_at DESC, verification_id DESC",
                verificationMapper,
                needId);
    }

    public List<NeedVerification> findByDepartmentId(int departmentId) {
        return jdbcTemplate.query(
                "SELECT * FROM need_verification WHERE department_id = ? ORDER BY verified_at DESC, verification_id DESC",
                verificationMapper,
                departmentId);
    }

    public List<NeedVerification> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM need_verification ORDER BY verified_at DESC, verification_id DESC",
                verificationMapper);
    }
}