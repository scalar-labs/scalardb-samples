package sample.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbHelperRepository;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Statement;

@Transactional
@Repository
public interface StatementRepository
    extends PagingAndSortingRepository<Statement, Integer>, ScalarDbHelperRepository<Statement> {

  List<Statement> findAllByOrderId(String orderId);
}
