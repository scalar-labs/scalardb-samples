package sample.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Customer;

@Transactional
@Repository
public interface CustomerRepository extends ScalarDbRepository<Customer, Integer> {

  default Customer getById(int id) {
    Optional<Customer> entity = findById(id);
    if (!entity.isPresent()) {
      // If the customer info the specified customer ID doesn't exist, throw an exception
      throw new RuntimeException(String.format("Customer not found. id:%d", id));
    }
    return entity.get();
  }

  default void insertIfNotExists(Customer customer) {
    if (!findById(customer.customerId).isPresent()) {
      insert(customer);
    }
  }
}
