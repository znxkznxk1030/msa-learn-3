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

import arthur.kim.api.product.Product;
import arthur.kim.api.recommendation.Recommendation;
import arthur.kim.api.review.Review;
import arthur.kim.microservices.core.product.composite.services.ProductCompositeIntegration;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.exceptions.NotFoundException;


// @RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
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
		when(compositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
		when(compositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));
		
		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
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
//    		.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
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
//    		.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
    		.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }
    
    

}

