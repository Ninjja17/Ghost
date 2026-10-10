package com.shadowengineer.api.profile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcUserProfileRepository implements UserProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserProfile save(UserProfile profile) {
        String sql = """
                insert into users (id, name, email, target_role, experience_level, growth_goal)
                values (?, ?, ?, ?, ?, ?)
                on conflict (id) do update set
                    name = excluded.name,
                    email = excluded.email,
                    target_role = excluded.target_role,
                    experience_level = excluded.experience_level,
                    growth_goal = excluded.growth_goal,
                    updated_at = now()
                """;
        jdbcTemplate.update(sql, profile.id(), profile.name(), profile.email(), profile.targetRole(),
                profile.experienceLevel(), profile.growthGoal());
        return findById(profile.id()).orElse(profile);
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return jdbcTemplate.query("select * from users where id = ?", this::mapProfile, id)
                .stream()
                .findFirst();
    }

    private UserProfile mapProfile(ResultSet resultSet, int rowNumber) throws SQLException {
        return new UserProfile(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("target_role"),
                resultSet.getString("experience_level"),
                resultSet.getString("growth_goal"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class)
        );
    }
}
