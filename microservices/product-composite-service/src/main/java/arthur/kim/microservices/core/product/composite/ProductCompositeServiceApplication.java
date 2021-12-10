package arthur.kim.microservices.core.product.composite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

import static java.util.Collections.emptyList;

@SpringBootApplication
@ComponentScan("arthur.kim")
public class ProductCompositeServiceApplication {

//	@Value("${api.common.version}")
//	String apiVersion;
//	@Value("${api.common.title}")
//	String apiTitle;
//	@Value("${api.common.description}")
//	String apiDescription;
//	@Value("${api.common.termsOfServiceUrl}")
//	String apiTermsOfServiceUrl;
//	@Value("${api.common.license}")
//	String apiLicense;
//	@Value("${api.common.licenseUrl}")
//	String apiLicenseUrl;
//	@Value("${api.common.contact.name}")
//	String apiContactName;
//	@Value("${api.common.contact.url}")
//	String apiContactUrl;
//	@Value("${api.common.contact.email}")
//	String apiContactEmail;
//
//	@Bean
//	public Docket apiDocumentation() {
//		return new Docket(DocumentationType.SWAGGER_2).select()
//				.apis(basePackage("arthur.kim.microservices.composite.product")).paths(PathSelectors.any()).build()
//				.apiInfo(new ApiInfo(apiTitle, apiDescription, apiVersion, apiTermsOfServiceUrl,
//						new Contact(apiContactName, apiContactUrl, apiContactEmail), apiLicense, apiLicenseUrl,
//						emptyList()));
//	}
//	
	

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

}