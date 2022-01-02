package arthur.kim.microservices.core.review;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("arthur.kim")
public class ReviewServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

  private final Integer connectionPoolSize;

  @Autowired
  public ReviewServiceApplication(
      @Value("${spring.datasource.maximum-pool-size:10}") Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  @Bean
  public Scheduler jdbcScheduler() {
    LOG.info("Creates a jdbcScheduler with connectionPoolSize = " + connectionPoolSize);
    return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
  }

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);

    String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
    LOG.info("Connected to MySQL: " + mysqlUri);
  }

}
