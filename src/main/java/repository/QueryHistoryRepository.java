package repository;

import core.query.QueryHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class QueryHistoryRepository implements core.query.QueryHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QueryHistory> rowMapper = (rs, rowNum) -> {
        String statusStr = rs.getString("status");
        QueryHistory.Status status;
        switch (statusStr) {
            case "SUCCESS":
                status = QueryHistory.Status.SUCCESS;
                break;
            case "TIMEOUT":
                status = QueryHistory.Status.TIMEOUT;
                break;
            default:
                status = QueryHistory.Status.ERROR;
        }

        Timestamp ts = rs.getTimestamp("created_at");

        return new QueryHistory(
                rs.getLong("id"),
                rs.getInt("user_id"),
                rs.getString("sql_text"),
                rs.getObject("execution_time_ms", Integer.class),
                rs.getObject("rows_scanned", Integer.class),
                rs.getObject("rows_returned", Integer.class),
                rs.getString("index_used"),
                rs.getString("explain_result"),
                status,
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    public QueryHistoryRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(QueryHistory queryHistory) {
        String sql = "INSERT INTO query_history (user_id, sql_text, execution_time_ms, " +
                "rows_scanned, rows_returned, index_used, explain_result, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, queryHistory.getUserId());
            ps.setString(2, queryHistory.getSqlText());
            ps.setObject(3, queryHistory.getExecutionTimeMs());
            ps.setObject(4, queryHistory.getRowsScanned());
            ps.setObject(5, queryHistory.getRowsReturned());
            ps.setString(6, queryHistory.getIndexUsed());
            ps.setString(7, queryHistory.getExplainResult());
            ps.setString(8, queryHistory.getStatus().name());
            ps.setTimestamp(9, Timestamp.valueOf(queryHistory.getCreatedAt()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            queryHistory.setId(key.longValue());
        }
    }

    @Override
    public Optional<QueryHistory> findById(Long id) {
        try {
            QueryHistory history = jdbcTemplate.queryForObject(
                    "SELECT * FROM query_history WHERE id = ?",
                    rowMapper,
                    id
            );
            return Optional.ofNullable(history);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<QueryHistory> findByUserId(Integer userId, int limit, int offset) {
        return jdbcTemplate.query(
                "SELECT * FROM query_history WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?",
                rowMapper,
                userId, limit, offset
        );
    }

    @Override
    public int countByUserId(Integer userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM query_history WHERE user_id = ?",
                Integer.class,
                userId
        );
        return count != null ? count : 0;
    }

    @Override
    public void deleteOldRecords(Integer userId, int keepCount) {
        // 最新のkeepCount件以外を削除
        jdbcTemplate.update(
                "DELETE FROM query_history WHERE user_id = ? AND id NOT IN " +
                        "(SELECT id FROM (SELECT id FROM query_history WHERE user_id = ? " +
                        "ORDER BY created_at DESC LIMIT ?) AS recent)",
                userId, userId, keepCount
        );
    }
}
