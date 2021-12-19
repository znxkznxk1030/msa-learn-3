package arthur.kim.microservices.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import arthur.kim.api.recommendation.Recommendation;
import arthur.kim.microservices.core.recommendation.persistence.RecommendationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class RecommendationServiceApplicationTests {
  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplicationTests.class);

  @Autowired
  private WebTestClient client;

  @Autowired
  private RecommendationRepository repository;

	@Test
	void contextLoads() {
	}

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();
  }

  @Test
  public void getRecommendationsByProductId() {
    int productId = 1;

    postAndVerifyRecommendation(productId, 1, OK);
    postAndVerifyRecommendation(productId, 2, OK);
    postAndVerifyRecommendation(productId, 3, OK);

    assertEquals(3, repository.findByProductId(productId).size());
    
    WebTestClient.BodyContentSpec spec = getAndVerifyRecommendationsByProductId(productId, OK);
    // String path = spec.json("$.path").toString();

    // String message = spec.json( "$.message").toString();
        
    // LOG.debug("path: {}", path);
    // LOG.debug("message: {}", message);

  }

  @Test
  public void duplicateError() {
    int productId = 1;
    int recommendationId = 1;

    postAndVerifyRecommendation(productId, recommendationId, OK);

    // LOG.debug(repository.findByProductId(productId));
    postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY);

    assertEquals(1, repository.count());
  }

  @Test
  public void deleteRecommendations() {
    int productId = 1;
    int recommendationId = 1;

    postAndVerifyRecommendation(productId, recommendationId, OK);
    assertEquals(1, repository.findByProductId(productId).size());

    deleteAndVerifyRecommendationsByProductId(productId, OK);
    assertEquals(0, repository.findByProductId(productId).size());

    deleteAndVerifyRecommendationsByProductId(productId, OK);
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

  private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/recommendation" + productIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
		return client.post()
			.uri("/recommendation")
			.body(just(recommendation), Recommendation.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/recommendation?productId=" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

}
