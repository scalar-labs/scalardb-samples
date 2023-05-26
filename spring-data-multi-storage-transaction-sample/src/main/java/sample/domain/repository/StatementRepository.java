package sample.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Statement;

@Transactional
@Repository
public interface StatementRepository extends ScalarDbRepository<Statement, Integer> {

  List<Statement> findAllByOrderId(String orderId);
}
