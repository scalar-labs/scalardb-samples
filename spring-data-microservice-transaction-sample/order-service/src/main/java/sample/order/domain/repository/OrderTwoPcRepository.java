package sample.order.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.order.domain.model.Order;

@Transactional(transactionManager = "scalarDbSuspendableTransactionManager")
@Repository
public interface OrderTwoPcRepository extends ScalarDbTwoPcRepository<Order, String> {
}
