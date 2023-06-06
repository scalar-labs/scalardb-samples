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

  // TODO: Using mixed Datasources to use both normal and 2PC transaction modes would be better
  //       like https://github.com/scalar-labs/scalardb-sql/tree/main/spring-data/example/mixed-transaction-modes
  //       so that we don't need to use this method. Or check auto-commit configuration later
  //
  // TODO: Maybe This API should be moved to ScalarDbTwoPcRepository.
  default <T> T execOneshotOperation(Supplier<T> task) {
    begin();
    T result = task.get();
    prepare();
    validate();
    commit();
    return result;
  }
}
