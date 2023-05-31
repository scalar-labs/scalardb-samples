package sample.order.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.order.domain.model.Order;

@Transactional
@Repository
public interface OrderRepository extends ScalarDbRepository<Order, String> {
  List<Order> findAllByCustomerIdOrderByTimestampDesc(int customerId);
}
