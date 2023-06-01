package sample.customer.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.customer.domain.model.Customer;

@Transactional(transactionManager = "scalarDbSuspendableTransactionManager")
@Repository
public interface CustomerTwoPcRepository extends ScalarDbTwoPcRepository<Customer, Integer> {
  // TODO: Using mixed Datasources to use both normal and 2PC transaction modes would be better
  //       like https://github.com/scalar-labs/scalardb-sql/tree/main/spring-data/example/mixed-transaction-modes
  default void insertIfNotExists(Customer customer) {
    begin();
    if (!findById(customer.customerId).isPresent()) {
      insert(customer);
    }
    prepare();
    validate();
    commit();
  }
}
