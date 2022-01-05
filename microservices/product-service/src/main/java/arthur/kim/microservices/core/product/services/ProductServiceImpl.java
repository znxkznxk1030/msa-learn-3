package arthur.kim.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoWriteException;

import arthur.kim.api.product.Product;
import arthur.kim.api.product.ProductService;
import arthur.kim.microservices.core.product.persistence.ProductEntity;
import arthur.kim.microservices.core.product.persistence.ProductRepository;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.exceptions.NotFoundException;
import arthur.kim.util.http.ServiceUtil;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

import java.util.function.Function;

@RestController
public class ProductServiceImpl implements ProductService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
  private final ServiceUtil serviceUtil;
  private final ProductRepository repository;
  private final ProductMapper mapper;

  @Autowired
  public ProductServiceImpl(ServiceUtil serviceUtil, ProductMapper mapper, ProductRepository repository) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    if (productId < 1)
      throw new InvalidInputException("Invalid productId: " + productId);

    return repository.findByProductId(productId)
        .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
        .log()
        .map(e -> mapper.entityToApi(e))
        .map(e -> {
          e.setServiceAddress(serviceUtil.getServiceAddress());
          return e;
        });
  }

   @Override
   public Product createProduct(Product body) {
	   if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
	   
	   ProductEntity entity = mapper.apiToEntity(body);
	   Mono<Product> newEntity = repository.save(entity)
			   .log()
			   .onErrorMap(DuplicateKeyException.class,
					   ex -> new InvalidInputException("Duplicated key, Product Id: " + body.getProductId()))
			   .map(e -> mapper.entityToApi(e));
	   
	   return newEntity.block();
			   
   }

   @Override
   public void deleteProduct(int productId) {
	   
	   if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
	   
	   repository.findByProductId(productId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();

   }

}
