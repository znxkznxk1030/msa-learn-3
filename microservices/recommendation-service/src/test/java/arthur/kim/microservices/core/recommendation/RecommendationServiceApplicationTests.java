package arthur.kim.microservices.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;

import arthur.kim.api.event.Event;
import arthur.kim.api.product.Product;
import arthur.kim.api.recommendation.Recommendation;
import arthur.kim.microservices.core.recommendation.persistence.RecommendationRepository;
import arthur.kim.util.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static arthur.kim.api.event.Event.Type.CREATE;
import static arthur.kim.api.event.Event.Type.DELETE;;

// @ExtendWith(SpringExtension.class)
// @ExtendWith(SpringExtension.class)
// @DataMongoTest
// @WebAppConfiguration
// @EnableSpringDataWebSupport
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.data.mongodb.port: 0" })
class RecommendationServiceApplicationTests {
  @Autowired
  private WebTestClient client;

  @Autowired
  private RecommendationRepository repository;

  @Autowired
  private Sink channels;

  private AbstractMessageChannel input = null;


  @BeforeEach
  public void setupDb() {
    input = (AbstractMessageChannel) channels.input();
    repository.deleteAll();
  }

  @Test
  public void getRecommendationsByProductId() {
    int productId = 1;

    sendCreateRecommendationEvent(productId, 1);
    sendCreateRecommendationEvent(productId, 2);
    sendCreateRecommendationEvent(productId, 3);

    assertEquals(3, (long) repository.findByProductId(productId).count().block());

    // String message = spec.json( "$.message").toString();

    // LOG.debug("path: {}", path);
    // LOG.debug("message: {}", message);

  }

  @Test
  public void duplicateError() {
    int productId = 1;
    int recommendationId = 1;

    sendCreateRecommendationEvent(productId, recommendationId);

    assertEquals(1, (long) repository.count().block());

    try {
      sendCreateRecommendationEvent(productId, recommendationId);
      fail("Expected a MessagingException here!");
    } catch (MessagingException me) {
      if (me.getCause() instanceof InvalidInputException) {
        InvalidInputException iie = (InvalidInputException) me.getCause();
        assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", iie.getMessage());
      } else {
        fail("Expected a InvalidInputException as the root cause!");
      }
    }

    assertEquals(1, (long) repository.count().block());
  }

  @Test
  public void deleteRecommendations() {
    int productId = 1;
    int recommendationId = 1;

    sendCreateRecommendationEvent(productId, recommendationId);
    assertEquals(1, (long) repository.findByProductId(productId).count().block());

    sendDeleteRecommendationEvent(productId);
    assertEquals(0, (long) repository.findByProductId(productId).count().block());

    sendDeleteRecommendationEvent(productId);
  }

  @Test
  public void getRecommendationsInvalidParameter() {
    getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST);
  }

  @Test
  public void getRecommendationsNotFound() {
    getAndVerifyRecommendationsByProductId("?productId=113", OK);
  }

  @Test
  public void getRecommendationsInvalidParameterNegativeValue() {
    int productIdInvalid = -1;
    getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY);
  }

  private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery,
      HttpStatus expectedStatus) {
    return client.get()
        .uri("/recommendation" + productIdQuery)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private void sendCreateRecommendationEvent(int productId, int recommendationId) {
    Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId,
        recommendationId, "Content " + recommendationId, "SA");
    Event<Integer, Product> event = new Event(CREATE, productId, recommendation);
    input.send(new GenericMessage<>(event));
  }

  private void sendDeleteRecommendationEvent(int productId) {
    Event<Integer, Product> event = new Event(DELETE, productId, null);
    input.send(new GenericMessage<>(event));
  }
}
