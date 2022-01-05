package arthur.kim.microservices.core.product;

//import static org.junit.jupiter.api.Assertions.assertEquals;
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

import arthur.kim.api.product.Product;
import arthur.kim.microservices.core.product.persistence.ProductEntity;
import arthur.kim.microservices.core.product.persistence.ProductRepository;
import reactor.test.StepVerifier;

import static java.util.stream.IntStream.rangeClosed;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@WebAppConfiguration
// @TestInstance(Lifecycle.PER_CLASS)
@EnableSpringDataWebSupport
public class PersistenceTests {

  @Autowired
  private ProductRepository repository;

  private ProductEntity savedEntity;

  @BeforeEach
  public void setupDb() {
	StepVerifier.create(repository.deleteAll()).verifyComplete();

    ProductEntity entity = new ProductEntity(1, "n", 1);
    StepVerifier.create(repository.save(entity))
        .expectNextMatches(createdEntity -> {
          savedEntity = createdEntity;
          return areProductEqual(entity, savedEntity);
        }).verifyComplete();
  }

   @Test
   public void create() {
     ProductEntity newEntity = new ProductEntity(2, "n", 2);
     
     StepVerifier.create(repository.save(newEntity))
     	.expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
     	.verifyComplete();
     
     
     StepVerifier.create(repository.findById(newEntity.getId()))
     	.expectNextMatches(foundEntity -> areProductEqual(foundEntity, newEntity))
     	.verifyComplete();
     
     StepVerifier.create(repository.count()).expectNext(2l).verifyComplete();
   }

   @Test
   public void update() {
	   savedEntity.setName("n2");
	   StepVerifier.create(repository.save(savedEntity))
	   .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
	   .verifyComplete();
	   
	   StepVerifier.create(repository.findById(savedEntity.getId()))
	   .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n2"))
	   .verifyComplete();
   }

   @Test
   public void delete() {
	   StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
	   StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
   }

  @Test
  public void getByProductId() {
    StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
        .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
        .verifyComplete();
  }

   @Test
   public void duplicateError() {
	   ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
	   StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
   }

   @Test
   public void optimisticLockError() {

     // 데이터베이스에서 가져온 엔티티를 변수 2개에 저장한다.
     ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
     ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

     // 첫 번째 엔티티 객체를 업데이트 한다.
     entity1.setName("n1");
     repository.save(entity1).block();

     // 두 번째 엔티티 객체를 업데이트한다.
     // 두 번째 엔티티 객체의 버전이 낮으므로 실패한다.
     // 즉 낙관적 잠금 오류가 발생해 실패한다.
     StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();
     
     StepVerifier.create(repository.findById(savedEntity.getId()))
     .expectNextMatches(foundEntity -> 
    		 foundEntity.getVersion() == 1 && foundEntity.getName().equals("n1"))
     .verifyComplete();
   }


  private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
    return (expectedEntity.getId().equals(actualEntity.getId())) &&
        (expectedEntity.getVersion() == actualEntity.getVersion()) &&
        (expectedEntity.getProductId() == actualEntity.getProductId()) &&
        (expectedEntity.getName().equals(actualEntity.getName())) &&
        (expectedEntity.getWeight() == actualEntity.getWeight());
  }

}
