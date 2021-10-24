package arthur.kim.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import arthur.kim.api.product.*;
import arthur.kim.microservices.util.http.*;

@RestController
public class ProductServiceImpl implements ProductService {
  private final ServiceUtil serviceUtil;

  @Autowired
  public ProductServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Product getProduct(int productId) {
    return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
  }

}
