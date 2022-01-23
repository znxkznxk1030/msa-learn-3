package arthur.kim.microservices.core.product.composite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("arthur.kim")
public class ProductCompositeServiceApplication {

  public static void main(String[] args) {

    SpringApplication.run(ProductCompositeServiceApplication.class, args);
  }
}