package com.relief.dao;

import com.relief.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setMobileNumber(rs.getString("mobile_number"));
        u.setName(rs.getString("name"));
        u.setRole(rs.getString("role"));
        u.setLatitude(rs.getDouble("latitude"));
        u.setLongitude(rs.getDouble("longitude"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    };

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY created_at DESC", userMapper);
    }

    public User findByMobile(String mobileNumber) {
        List<User> users = jdbcTemplate.query(
            "SELECT * FROM users WHERE mobile_number = ?",
            userMapper,
            mobileNumber
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public int save(User user) {
        return jdbcTemplate.update(
            "INSERT INTO users (mobile_number, name, role, latitude, longitude) VALUES (?, ?, ?, ?, ?)",
            user.getMobileNumber(),
            user.getName(),
            user.getRole(),
            user.getLatitude(),
            user.getLongitude()
        );
    }
}
