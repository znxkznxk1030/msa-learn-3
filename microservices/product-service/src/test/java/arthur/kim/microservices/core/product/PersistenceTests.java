package arthur.kim.microservices.core.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;

import arthur.kim.microservices.core.product.persistence.ProductEntity;
import arthur.kim.microservices.core.product.persistence.ProductRepository;

@DataMongoTest
//@TestInstance(Lifecycle.PER_CLASS)
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
	
	
	private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
		assertEquals(expectedEntity.getId(),			actualEntity.getId());
		assertEquals(expectedEntity.getVersion(),		actualEntity.getVersion());
		assertEquals(expectedEntity.getProductId(),		actualEntity.getProductId());
		assertEquals(expectedEntity.getName(),			actualEntity.getName());
		assertEquals(expectedEntity.getWeight(),		actualEntity.getWeight());
	}

}
