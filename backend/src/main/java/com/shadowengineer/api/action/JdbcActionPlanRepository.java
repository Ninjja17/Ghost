package com.shadowengineer.api.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcActionPlanRepository implements ActionPlanRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcActionPlanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ActionPlanItem> findByUserId(UUID userId) {
        return jdbcTemplate.query("select * from action_plan_items where user_id = ? order by week_number", this::mapItem, userId);
    }

    @Override
    public ActionPlanItem save(ActionPlanItem item) {
        String sql = """
                insert into action_plan_items (id, user_id, week_number, title, details, completion_criteria, status, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do update set status = excluded.status, updated_at = excluded.updated_at
                """;
        jdbcTemplate.update(sql, item.id(), item.userId(), item.weekNumber(), item.title(), item.details(),
                item.completionCriteria(), item.status(), item.createdAt(), item.updatedAt());
        return item;
    }

    private ActionPlanItem mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        return new ActionPlanItem(
                resultSet.getObject("id", UUID.class), resultSet.getObject("user_id", UUID.class),
                resultSet.getInt("week_number"), resultSet.getString("title"), resultSet.getString("details"),
                resultSet.getString("completion_criteria"), resultSet.getString("status"),
                resultSet.getObject("created_at", OffsetDateTime.class), resultSet.getObject("updated_at", OffsetDateTime.class)
        );
    }
}
