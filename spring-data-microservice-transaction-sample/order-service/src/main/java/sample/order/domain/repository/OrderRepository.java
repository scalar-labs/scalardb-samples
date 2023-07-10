package sample.order.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.order.domain.model.Order;

@Transactional
@Repository
public interface OrderRepository extends ScalarDbTwoPcRepository<Order, String> {

  List<Order> findAllByCustomerIdOrderByTimestampDesc(int customerId);
}
