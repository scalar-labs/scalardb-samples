package sample.order.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.order.domain.model.Statement;

@Transactional
@Repository
public interface StatementRepository extends ScalarDbTwoPcRepository<Statement, Integer> {

  List<Statement> findAllByOrderId(String orderId);
}
