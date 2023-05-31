package sample.order.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.order.domain.model.Item;

@Transactional
@Repository
public interface ItemRepository extends ScalarDbRepository<Item, Integer> {
  default void insertIfNotExists(Item item) {
    if (!findById(item.itemId).isPresent()) {
      insert(item);
    }
  }
}
