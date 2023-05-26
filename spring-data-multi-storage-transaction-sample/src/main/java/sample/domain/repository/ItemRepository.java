package sample.domain.repository;

import com.scalar.db.sql.springdata.ScalarDbRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Item;

@Transactional
@Repository
public interface ItemRepository extends ScalarDbRepository<Item, Integer> {

  default Item getById(int id) {
    Optional<Item> entity = findById(id);
    if (!entity.isPresent()) {
      throw new RuntimeException(String.format("Item not found. id:%d", id));
    }
    return entity.get();
  }

  default void insertIfNotExists(Item item) {
    if (!findById(item.itemId).isPresent()) {
      insert(item);
    }
  }
}
