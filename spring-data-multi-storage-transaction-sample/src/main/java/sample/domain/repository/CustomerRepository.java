package sample.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbHelperRepository;
import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Customer;

@Transactional
@Repository
public interface CustomerRepository
    extends PagingAndSortingRepository<Customer, Integer>, ScalarDbHelperRepository<Customer> {

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
