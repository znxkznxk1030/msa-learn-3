package arthur.kim.microservices.core.product;

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
import arthur.kim.microservices.core.product.persistence.ProductRepository;
import arthur.kim.util.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static arthur.kim.api.event.Event.Type.CREATE;
import static arthur.kim.api.event.Event.Type.DELETE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ProductRepository repository;

  @Autowired
  private Sink channels;

  private AbstractMessageChannel input = null;

	@Test
	void contextLoads() {
	}

  @BeforeEach
  public void setupDb() {
    input = (AbstractMessageChannel) channels.input();
    repository.deleteAll().block();
  }

  @Test
  public void getProductById() {
    int productId = 1;

    sendCreateProductEvent(productId);

    getAndVerifyProduct(productId, OK)
      .jsonPath("$.productId").isEqualTo(productId);
  }

 @Test
 public void duplicatedError() {
   int productId = 1;

   sendCreateProductEvent(productId);


   try {
     sendCreateProductEvent(productId);
     fail("Expected a MessagingException here!");
   } catch (MessagingException me) {
     if (me.getCause() instanceof InvalidInputException) {
       InvalidInputException iie = (InvalidInputException) me.getCause();
       assertEquals("Duplicated key, Product Id: " + productId, iie.getMessage());
     } else {
       fail("Expected a InvalidInputException as the root cause!");
     }
   }
   
 }

 @Test
 public void deleteProduct() {
   int productId = 1;

   sendCreateProductEvent(productId);
   assertNotNull(repository.findByProductId(productId).block());

   sendDeleteProductEvent(productId);
   assertNull(repository.findByProductId(productId).block());

   sendDeleteProductEvent(productId);
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

  // private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
  //   Product product = new Product(productId, "Name " + productId, productId, "SA");
  //   return client.post().uri("/product")
  //       .body(just(product), Product.class)
  //       .accept(APPLICATION_JSON)
  //       .exchange()
  //       .expectStatus().isEqualTo(expectedStatus)
  //       .expectHeader().contentType(APPLICATION_JSON)
  //       .expectBody();
  // }

  // private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
  //   return client.delete().uri("/product/" + productId)
  //       .accept(APPLICATION_JSON)
  //       .exchange()
  //       .expectStatus().isEqualTo(expectedStatus)
  //       .expectBody();
  // }

  private void sendCreateProductEvent(int productId) {
    Product product = new Product(productId, "Name " + productId, productId, "SA");
    Event<Integer, Product> event = new Event(CREATE, productId, product);
    input.send(new GenericMessage<>(event));
  }

  private void sendDeleteProductEvent(int productId) {
    Event<Integer, Product> event = new Event(DELETE, productId, null);
    input.send(new GenericMessage<>(event));
  }
}
