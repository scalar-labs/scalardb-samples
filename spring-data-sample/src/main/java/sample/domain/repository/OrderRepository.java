package sample.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Order;

@Transactional
@Repository
public interface OrderRepository extends ScalarDbRepository<Order, String> {

  default Order getById(String id) {
    Optional<Order> entity = findById(id);
    if (!entity.isPresent()) {
      throw new RuntimeException(String.format("Order not found. id:%s", id));
    }
    return entity.get();
  }

  List<Order> findAllByCustomerIdOrderByTimestampDesc(int customerId);
}
