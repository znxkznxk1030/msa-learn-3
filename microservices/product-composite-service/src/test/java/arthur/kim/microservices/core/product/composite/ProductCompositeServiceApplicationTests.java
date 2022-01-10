package arthur.kim.microservices.core.product.composite;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import arthur.kim.api.composite.product.ProductAggregate;
import arthur.kim.api.composite.product.RecommendationSummary;
import arthur.kim.api.composite.product.ReviewSummary;
import arthur.kim.api.product.Product;
import arthur.kim.api.recommendation.Recommendation;
import arthur.kim.api.review.Review;
import arthur.kim.microservices.core.product.composite.services.ProductCompositeIntegration;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.just;
import static org.springframework.http.MediaType.APPLICATION_JSON;

// @RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_INVALID = 3;

  @Autowired
  private WebTestClient client;

  @MockBean
  private ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  public void setup() {

    when(compositeIntegration.getProduct(PRODUCT_ID_OK))
        .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));
    /*
     * Collections.singletonList()
     * 변경여부 : immutable (불변)
     * 사이즈 : size가 1로 고정됨(지정된 단일 객체를 가르키는 주소값을 가지기 때문)
     * 값 및 구조적 변경 시 UnsupportedOperationException 발생
     */
    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
        .thenReturn(Flux.fromIterable(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));
    when(compositeIntegration.getReviews(PRODUCT_ID_OK))
        .thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
        .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
    when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
        .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
  }

  @Test
  void contextLoads() {
  }

  @Test
  public void getProductId() {
    client.get()
        .uri("/product-composite/" + PRODUCT_ID_OK)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
        .jsonPath("$.recommendations.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  @Test
  public void getProductNotFound() {
    client.get()
        .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        // .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  public void getProductInvalidInput() {
    client.get()
        .uri("/product-composite/" + PRODUCT_ID_INVALID)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        // .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
        .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
  }

  @Test
  public void createCompositeProduct1() {
    ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);
    postAndVerifyProduct(compositeProduct, HttpStatus.OK);
  }

  @Test
  public void createCompositeProduct2() {
    ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
        singletonList(new RecommendationSummary(1, "a", 1, "c")),
        singletonList(new ReviewSummary(1, "a", "b", "c")), null);
    postAndVerifyProduct(compositeProduct, HttpStatus.OK);
  }

  @Test
  public void deleteCompositeProduct() {
    ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
        singletonList(new RecommendationSummary(1, "a", 1, "c")),
        singletonList(new ReviewSummary(1, "a", "b", "c")), null);
    postAndVerifyProduct(compositeProduct, HttpStatus.OK);
    deleteAndVerifyProduct(1, HttpStatus.OK);
    deleteAndVerifyProduct(1, HttpStatus.OK);
  }

  private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
    client.post()
        .uri("/product-composite")
        .accept(APPLICATION_JSON)
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
