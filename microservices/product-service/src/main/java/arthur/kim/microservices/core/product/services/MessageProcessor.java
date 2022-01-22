package arthur.kim.microservices.core.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import arthur.kim.api.event.Event;
import arthur.kim.api.product.Product;
import arthur.kim.api.product.ProductService;
import arthur.kim.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

  private final ProductService productService;

  @Autowired
  public MessageProcessor(ProductService productService) {
    this.productService = productService;
  }

  @StreamListener(target = Sink.INPUT)
  public void process(Event<Integer, Product> event) {

    switch (event.getEventType()) {
      case CREATE:
        Product product = event.getData();
        productService.createProduct(product);
        break;
      case DELETE:
        int productId = event.getKey();
        productService.deleteProduct(productId);
        break;
      default:
        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
        throw new EventProcessingException(errorMessage);
    }

  }

}
