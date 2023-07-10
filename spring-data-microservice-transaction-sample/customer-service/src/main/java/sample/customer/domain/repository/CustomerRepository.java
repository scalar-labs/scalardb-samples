package sample.customer.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
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
}
