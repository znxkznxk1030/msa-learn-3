package arthur.kim.microservices.core.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import arthur.kim.microservices.core.product.persistence.ProductEntity;
import arthur.kim.microservices.core.product.persistence.ProductRepository;

import static java.util.stream.IntStream.rangeClosed;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@WebAppConfiguration
//@TestInstance(Lifecycle.PER_CLASS)
@EnableSpringDataWebSupport
public class PersistenceTests {
	
	@Autowired
	private ProductRepository repository;
	
	private ProductEntity savedEntity;
	
	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
		
		ProductEntity entity = new ProductEntity(1, "n", 1);
		savedEntity = repository.save(entity);
		
		assertEqualsProduct(entity, savedEntity);
	}
	
	@Test
	public void create() {
		ProductEntity newEntity = new ProductEntity(2, "n", 2);
		savedEntity = repository.save(newEntity);
		
		ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
		assertEqualsProduct(newEntity, foundEntity);
		
		assertEquals(2, repository.count());
	}
	
	@Test
	public void update() {
		savedEntity.setName("n2");
		repository.save(savedEntity);
		
		ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
		
		assertEquals(1, (long)foundEntity.getVersion());
		assertEquals("n2", foundEntity.getName());
	}
	
	@Test
	public void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()));
	}
	
	@Test
	public void getByProductId() {
		Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());
		assertTrue(entity.isPresent());
		assertEqualsProduct(savedEntity, entity.get());
	}
	
	@Test
	public void duplicateError() {
		assertThrows(DuplicateKeyException.class, () -> {
			ProductEntity entity2 = new ProductEntity(savedEntity.getProductId(), "n", 1);
			repository.save(entity2);
		});
	}
	
	@Test
	public void optimisticLockError() {
		
		// 데이터베이스에서 가져온 엔티티를 변수 2개에 저장한다.
		ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
		ProductEntity entity2 = repository.findById(savedEntity.getId()).get();
		
		// 첫 번째 엔티티 객체를 업데이트 한다.
		entity1.setName("n1");
		repository.save(entity1);
		
		// 두 번째 엔티티 객체를 업데이트한다.
		// 두 번째 엔티티 객체의 버전이 낮으므로 실패한다.
		// 즉 낙관적 잠금 오류가 발생해 실패한다.
		try {
			entity2.setName("n2");
			repository.save(entity2);
			
			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException e) {}
		
		// 데이터베이스에서 업데이트된 엔티티를 가져와서 새로운 값을 확인한다.
		ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (int)updatedEntity.getVersion());
		assertEquals("n1", updatedEntity.getName());
	}
	
	@Test
	public void paging() {
		repository.deleteAll();
		List<ProductEntity> newProducts = rangeClosed(1001, 1010)
				.mapToObj(i -> new ProductEntity(i, "name " + i, i)).collect(Collectors.toList());
		repository.saveAll(newProducts);
		
		PageRequest nextPage = PageRequest.of(0, 4, Direction.ASC, "productId");
		Page<ProductEntity> productPage = repository.findAll(nextPage);
		assertEquals("[1001, 1002, 1003, 1004]", productPage.getContent().stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());
		assertEquals(true, productPage.hasNext());
//		nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
//		nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
//		nextPage = testNextPage(nextPage, "[1009, 1010]", false);
				
	}
	
	private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
		assertEquals(expectedEntity.getId(),			actualEntity.getId());
		assertEquals(expectedEntity.getVersion(),		actualEntity.getVersion());
		assertEquals(expectedEntity.getProductId(),		actualEntity.getProductId());
		assertEquals(expectedEntity.getName(),			actualEntity.getName());
		assertEquals(expectedEntity.getWeight(),		actualEntity.getWeight());
	}

}
