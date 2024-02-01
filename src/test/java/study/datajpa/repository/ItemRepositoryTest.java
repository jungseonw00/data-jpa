package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

@SpringBootTest
public class ItemRepositoryTest {

	@Autowired ItemRepository itemRepository;

	 @Test
	 void save() {
	     // given
	     Item item = new Item();
		 itemRepository.save(item);
	 }
}
