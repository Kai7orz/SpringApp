package core.query;

import java.util.List;
import java.util.Optional;

public interface QueryHistoryRepository {
    void save(QueryHistory queryHistory);
    Optional<QueryHistory> findById(Long id);
    List<QueryHistory> findByUserId(Integer userId, int limit, int offset);
    int countByUserId(Integer userId);
    void deleteOldRecords(Integer userId, int keepCount);
}
