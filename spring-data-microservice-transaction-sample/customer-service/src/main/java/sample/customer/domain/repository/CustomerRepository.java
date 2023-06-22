package sample.customer.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import java.util.function.Supplier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.customer.domain.model.Customer;

@Transactional
@Repository
public interface CustomerRepository extends ScalarDbTwoPcRepository<Customer, Integer> {

  default void insertIfNotExists(Customer customer) {
    if (!findById(customer.customerId).isPresent()) {
      insert(customer);
    }
  }

  default <T> T execOneshotOperation(Supplier<T> task) {
    begin();
    T result = task.get();
    prepare();
    validate();
    commit();
    return result;
  }

  default <T> T execBatchOperations(Supplier<T> task) {
    return task.get();
  }
}
