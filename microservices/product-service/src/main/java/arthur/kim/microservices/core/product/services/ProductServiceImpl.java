package arthur.kim.microservices.core.product.services;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DuplicateKeyException;

import arthur.kim.api.product.Product;
import arthur.kim.api.product.ProductService;
import arthur.kim.microservices.core.product.persistence.ProductEntity;
import arthur.kim.microservices.core.product.persistence.ProductRepository;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.exceptions.NotFoundException;
import arthur.kim.util.http.ServiceUtil;

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
  public Product getProduct(int productId) {
	LOG.debug("/product return the found product for productId={}", productId);
	
	if ( productId < 1 ) throw new InvalidInputException("Invalid productId: " + productId);
	
	ProductEntity entity = repository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No Product found for productId: " + productId));
		
	Product response = mapper.entityToApi(entity);
	response.setServiceAddress(serviceUtil.getServiceAddress());
	return response;
  }

  @Override
  public Product createProduct(Product body) {
	  try {
		  ProductEntity entity = mapper.apiToEntity(body);
		  ProductEntity newEntity = repository.save(entity);
		  return mapper.entityToApi(newEntity);
	  } catch (DuplicateKeyException dke) {
		  throw new InvalidInputException("Duplicate Key, Product Id: " + body.getProductId());
	  }
  }

  @Override
  public void deleteProduct(int productId) {
	  // TODO Auto-generated method stub
	  repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
	
  }

}
