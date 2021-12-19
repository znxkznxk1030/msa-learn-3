package arthur.kim.microservices.core.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import arthur.kim.api.review.Review;
import arthur.kim.microservices.core.review.persistence.ReviewRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:review-db"})
class ReviewServiceApplicationTests {

	@Test
	void contextLoads() {
	}

  @Autowired
  private WebTestClient client;

  @Autowired
  private ReviewRepository repository;

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();
  }

  @Test
  public void getReviewsByProductId() {
    int productId = 1;

    assertEquals(0, repository.findByProductId(productId).size());

    postAndVerifyReview(productId, 1, OK);
    postAndVerifyReview(productId, 2, OK);
    postAndVerifyReview(productId, 3, OK);

    assertEquals(3, repository.findByProductId(productId).size());

    getAndVerifyReviewsByProductId(1, OK);
  }

  @Test
  public void duplicateError() {
    int productId = 1;
    int reviewId = 1;

    assertEquals(0, repository.count());

    postAndVerifyReview(productId, reviewId, OK);

    assertEquals(1, repository.count());

    postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY);
  }

  @Test
  public void deleteReviews() {

    int productId = 1;
    int reviewId = 1;

    postAndVerifyReview(productId, reviewId, OK);
    assertEquals(1, repository.findByProductId(productId).size());

    deleteAndVerifyReviewsByProductId(productId, OK);
    assertEquals(0, repository.findByProductId(productId).size());

    deleteAndVerifyReviewsByProductId(productId, OK);
  }

  @Test
  public void getReviewsMessingParameter() {
    getAndVerifyReviewsByProductId("", BAD_REQUEST);
  }

  @Test
  public void getReviewsInvalidParameter() {
    getAndVerifyReviewsByProductId("?productID=no-integer", BAD_REQUEST);
  }

  @Test
  public void getReviewsNotFound() {
    getAndVerifyReviewsByProductId("?productId=213", OK);
  }

  @Test
  public void getReviewsInvalidParameterNegativeValue() {

  }

  private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
    return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
    return client.get()
      .uri("/review" + productIdQuery)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
    Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
    return client.post()
        .uri("/review")
        .body(just(review), Review.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
    return client.delete()
        .uri("/review?productId=" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectBody();
  }


}
