package arthur.kim.microservices.core.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import arthur.kim.microservices.core.product.persistence.ProductEntity;

@SpringBootApplication
@ComponentScan("arthur.kim")
@EnableReactiveMongoRepositories
public class ProductServiceApplication {
  public static void main(String[] args) {
    // ConfigurableApplicationContext ctx =
    SpringApplication.run(ProductServiceApplication.class, args);

//     String mongodDbHost =
//     ctx.getEnvironment().getProperty("spring.data.mongodb.host");
//     String mongodDbPort =
//     ctx.getEnvironment().getProperty("spring.data.mongodb.port");
//     LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
  }
  
//  @Autowired
//  MongoOperations mongoTemplate;
//
//  @EventListener(ContextRefreshedEvent.class)
//  public void initIndicesAfterStartup() {
//
//	MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
//	IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
//
//	IndexOperations indexOps = mongoTemplate.indexOps(ProductEntity.class);
//	resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOps.ensureIndex(e));
//  }
}
