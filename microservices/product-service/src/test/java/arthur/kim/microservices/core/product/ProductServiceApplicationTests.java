package arthur.kim.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import arthur.kim.api.product.Product;
import arthur.kim.microservices.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ExtendWith(SpringExtension.class)
// @TestInstance(Lifecycle.PER_CLASS)
class ProductServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ProductRepository repository;

	@Test
	void contextLoads() {
	}

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();
  }

  @Test
  public void getProductById() {
    int productId = 1;

    postAndVerifyProduct(productId, OK);

    getAndVerifyProduct(productId, OK)
      .jsonPath("$.productId").isEqualTo(productId);
  }

  @Test
  public void duplicatedError() {
    int productId = 1;

    postAndVerifyProduct(productId, OK);


    postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY);
    // .jsonPath("$.path").isEqualTo("/product")
    // .jsonPath("$.message").isEqualTo("Duplicate Key, Product Id: " + productId);
    
  }

  @Test
  public void deleteProduct() {
    int productId = 1;

    postAndVerifyProduct(productId, OK);

    deleteAndVerifyProduct(productId, OK);

    deleteAndVerifyProduct(productId, OK);
  }

  @Test
  public void getProductInvalidParameterString() {
    getAndVerifyProduct("/no-integer", BAD_REQUEST);
        // .jsonPath("$.path").isEqualTo("/product/no-integer")
        // .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  public void getProductNotFound() {
    int productIdNotFound = 13;
		getAndVerifyProduct(productIdNotFound, NOT_FOUND);
  }

  @Test
  public void getProductInvalidParameterNegativeValue() {
    int productIdInvalid = -1;

		getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY);
    assertTrue(true);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    return getAndVerifyProduct("/" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
    return client.get()
      .uri("/product" + productIdPath)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    Product product = new Product(productId, "Name " + productId, productId, "SA");
    return client.post().uri("/product")
        .body(just(product), Product.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    return client.delete().uri("/product/" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectBody();
  }
}
