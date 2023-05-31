package sample.customer.domain.repository;

import com.scalar.db.sql.springdata.twopc.ScalarDbTwoPcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.customer.domain.model.Customer;

@Transactional(transactionManager = "scalarDbSuspendableTransactionManager")
@Repository
public interface CustomerTwoPcRepository extends ScalarDbTwoPcRepository<Customer, Integer> {
}
