package arthur.kim.microservices.core.product.composite;

import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.HttpStatus;

import arthur.kim.api.composite.product.ProductAggregate;
import arthur.kim.api.event.Event;
import arthur.kim.api.product.Product;
import arthur.kim.microservices.core.product.composite.services.ProductCompositeIntegration;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;
import static arthur.kim.api.event.Event.Type.CREATE;
import static arthur.kim.api.event.Event.Type.DELETE;
import static arthur.kim.microservices.core.product.composite.IsSameEvent.sameEventExceptCreatedAt;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MessagingTests {
  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_INVALID = 3;


  @Autowired
  private WebTestClient client;

  @Autowired
  private ProductCompositeIntegration.MessageSources channels;

  @Autowired
  private MessageCollector collector;

  BlockingQueue<Message<?>> queueProducts = null;
  BlockingQueue<Message<?>> queueRecommendations = null;
  BlockingQueue<Message<?>> queueReviews = null;

  @BeforeEach
  public void setUp() {
    queueProducts = getQueue(channels.outputProducts());
    queueRecommendations = getQueue(channels.outputRecommendations());
    queueReviews = getQueue(channels.outputReviews());
  }

  private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
    return collector.forChannel(messageChannel);
  }

  @Test
  public void createCompositeProduct1() {

    ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);
    postAndVerifyProduct(composite, OK);

    // Assert one expected new product events queued up
    assertEquals(1, queueProducts.size());

    Event<Integer, Product> expectedEvent = new Event(CREATE, composite.getProductId(),
        new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));
    assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

    // Assert none recommendations and review events
    assertEquals(0, queueRecommendations.size());
    assertEquals(0, queueReviews.size());
  }



  private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
    client.post()
        .uri("/product-composite")
        .body(just(compositeProduct), ProductAggregate.class)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus);
  }

  private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    client.delete()
        .uri("/product-composite/" + productId)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus);
  }
  
}
