package sample.order.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.order.domain.model.Order;

@Transactional
@Repository
public interface OrderRepository extends ScalarDbTwoPcRepository<Order, String> {

  List<Order> findAllByCustomerIdOrderByTimestampDesc(int customerId);

  default <T> T execOneshotOperation(Supplier<T> task) {
    begin();
    T result = task.get();
    prepare();
    validate();
    commit();
    return result;
  }
}
