package arthur.kim.microservices.core.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("arthur.kim")
public class ProductServiceApplication {
  public static void main(String[] args) {
    // ConfigurableApplicationContext ctx =
    SpringApplication.run(ProductServiceApplication.class, args);

    // String mongodDbHost =
    // ctx.getEnvironment().getProperty("spring.data.mongodb.host");
    // String mongodDbPort =
    // ctx.getEnvironment().getProperty("spring.data.mongodb.port");
    // LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
  }
}
