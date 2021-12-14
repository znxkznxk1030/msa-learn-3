package arthur.kim.microservices.core.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.DuplicateKeyException;

import arthur.kim.microservices.core.product.persistence.ProductEntity;
import arthur.kim.microservices.core.product.persistence.ProductRepository;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {
	
	@Autowired
	private ProductRepository repository;
	
	private ProductEntity savedEntity;
	
	@Before
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
		
	}
	
	@Test
	public void delete() {
		
	}
	
	@Test
	public void getByProductId() {
		
	}
	
	@Test(expected = DuplicateKeyException.class)
	public void duplicateError() {
		
	}
	
	
	private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
		assertEquals(expectedEntity.getId(),			actualEntity.getId());
		assertEquals(expectedEntity.getVersion(),		actualEntity.getVersion());
		assertEquals(expectedEntity.getProductId(),		actualEntity.getProductId());
		assertEquals(expectedEntity.getName(),			actualEntity.getName());
		assertEquals(expectedEntity.getWeight(),		actualEntity.getWeight());
	}

}
