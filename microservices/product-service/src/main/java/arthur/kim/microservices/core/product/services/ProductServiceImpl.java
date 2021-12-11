package arthur.kim.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import arthur.kim.api.product.Product;
import arthur.kim.api.product.ProductService;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.exceptions.NotFoundException;
import arthur.kim.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {
	
  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
  private final ServiceUtil serviceUtil;

  @Autowired
  public ProductServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Product getProduct(int productId) {
	LOG.debug("/product return the found product for productId={}", productId);
	
	if ( productId < 1 ) throw new InvalidInputException("Invalid productId: " + productId);
	if ( productId == 13 ) throw new NotFoundException("No product found for productId: " + productId); 
	
	
    return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
  }

}
