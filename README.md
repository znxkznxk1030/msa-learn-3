# Chapter 3

## gradle 설정

``` bash
buildscript {
  ext {
    springBootVersion = '2.1.0.RC1'
  }
  
  repositories {
    mavenCentral ()
    maven { url "https://repo.spring.io/snapshot"}
    maven { url "https://repo.spring.io/milestone"}
  }
  dependencies {
    classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
  }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-managerment'
```

- 스프링 부트 버전은 spring init 커맨드를 실행할 때 지정한 것으로 설정
- 스프링 프로젝트 저장소를 추가한 이유는 정식버전이 아닌 버전 (2.1.0.RC1)의 라이브러리는 중앙 메이븐 저장소에 없기 때문이다.

```bash
group = 'arthur.kim.microservices.core'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
  mavenCentral()
}

dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
  useJUnitPlatform()
}
```

###  커맨드로 각 마이크로서비스 빌드

``` bash
cd microservices/product-service; ./gradlew build; cd -; \
cd microservices/product-composite-service; ./gradlew build; cd -; \
cd microservices/recommendation-service; ./gradlew build; cd-; \
cd microservices/review-service; ./gradlew build; cd-;
```

## 그래들에 멀티 프로젝트 빌드 설정

### 1. settings.gradle 파일을 생성하고 그래들이 빌드할 프로젝트를 입력

``` gradle
include ':microservices:product-service'
include ':microservices:review-service'
include ':microservices:recommendation-service'
include ':microservices:product-composite-service'
```

### 2. Product-service 프로젝트에서 그래들 실행 파일을 복사

```bash
# cp: copy
cp -r microservices/product-service/gradle .; \
cp -r microservices/product-service/gradlew .; \
cp -r microservices/product-service/gradlew.bat .; \
cp -r microservices/product-service/.gitignore .; \
```

### 3. 각 프로젝트의 그래들 실행 파일 제거

```bash
# "{}" > find 로 찾은 파일명

# -r : recursive
# -f : force
# -v : verbose

find microservices -depth -name "gradle" -exec rm -rfv "{}" \;
find microservices -depth -name "gradlew" -exec rm -fv "{}" \;
```

### 4. 하나의 커맨드로 전체 마이크로서비스 빌드

```bash
./gradlew build
```

### 5. 마이크로 서비스 런

```bash
java -jar microservices/product-service/build/libs/product-service-0.0.1-SNAPSHOT.jar &

java -jar microservices/product-composite-service/build/libs/product-composite-service-0.0.1-SNAPSHOT.jar &


java -jar microservices/review-service/build/libs/review-service-0.0.1-SNAPSHOT.jar &

java -jar microservices/recommendation-service/build/libs/recommendation-service-0.0.1-SNAPSHOT.jar &


```

### api, util프로젝트는 각 프로젝트에서 빌드가 안되서 ide가 의미없음

### Gradle은 각 환경별 세팅을 맞추고 싶다면, gradle wrapper를 이용하면 좋다

### 포트 죽이기

```bash
kill $(jobs -p )
```

### @RestControllerAdvice 를 이용해서 예외처리하기

> @ExceptionHandler, @ModelAttribute, @InitBinder 가 적용된 메서드들을 AOP를 적용해 컨트롤러 단에 적용하기 위해 고안된 애너테이션

```java
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public @ResponseBody HttpErrorInfo handleNotFoundExceptions(ServerHttpRequest request, Exception ex) {
    return createHttpErrorInfo(NOT_FOUND, request, ex);
  }
  
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ExceptionHandler(InvalidInputException.class)
  public @ResponseBody HttpErrorInfo handelInvalidInputException(ServerHttpRequest request, Exception ex) {
    
    return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
  }
  
  private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {
    final String path = request.getPath().pathWithinApplication().value();
    final String message = ex.getMessage();
      
    LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
    return new HttpErrorInfo(httpStatus, path, message);
  }

}

```

### @ExceptionHandler

> @Controller, @RestController가 적용된 Bean내에서 발생하는 예외를 잡아서 하나의 메서드에서 처리

1. Controller, RestController에만 적용가능하다. (@Service같은 빈에서는 안됨.)
2. 리턴 타입은 자유롭게 해도 된다. (Controller내부에 있는 메서드들은 여러 타입의 response를 할 것이다. 해당 타입과 전혀다른 리턴 타입이어도 상관없다.)
3. @ExceptionHandler를 등록한 Controller에만 적용된다. 다른 Controller에서 NullPointerException이 발생하더라도 예외를 처리할 수 없다.
4. 메서드의 파라미터로 Exception을 받아왔는데 이것 또한 자유롭게 받아와도 된다.

### 테스트 목록

```bash
# 존재하지 않는 productId(13)를 조회하고 404(Not Found)가 반환되는지 검증
curl http://localhost:7000/product-composite/13 -i
```

### webflux 와 함께나온 WebTestClient는 요청을 보내고 결과를 검증하는 다양한 API를 제공한다

### Testcase 에서 안되는 점들

#### junit 5에서는 @RunWith(SpringRunner.class) 를 사용하지 않는다

#### junit 5에서는 @Before 대신 @BeforeAll 또는 @BeforeEach

#### WebEnvironment.RANDOM_PORT 는 restTemplate을 테스트하기 위함. ( Default는 WebEnvironment.MOCK )

#### Test 검증에 실패한 케이스의 경우 build시 오류로 출력된다. ( + 빌드 실패 )

``` bash
> Task :microservices:product-composite-service:test

ProductCompositeServiceApplicationTests > getProductId() FAILED
    java.lang.AssertionError: JSON path "$.recommendations.length()" expected:<0> but was:<1>
        at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
        at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
        at org.springframework.test.util.JsonPathExpectationsHelper.assertValue(JsonPathExpectationsHelper.java:125)
        at org.springframework.test.web.reactive.server.JsonPathAssertions.isEqualTo(JsonPathAssertions.java:54)
        at arthur.kim.microservices.core.product.composite.ProductCompositeServiceApplicationTests.getProductId(ProductCompositeServiceApplicationTests.java:57)

4 tests completed, 1 failed
Finished generating test XML results (0.025 secs) into: /Users/yskim/Desktop/msa-learn/microservices/product-composite-service/build/test-results/test
Generating HTML test report...
Finished generating test html results (0.021 secs) into: /Users/yskim/Desktop/msa-learn/microservices/product-composite-service/build/reports/tests/test

> Task :microservices:product-composite-service:test FAILED
:microservices:product-composite-service:test (Thread[Execution worker for ':' Thread 7,5,main]) completed. Took 12.034 secs.

FAILURE: Build failed with an exception.
```

- Test Result Dashboard

> file:///Users/yskim/Desktop/msa-learn/microservices/product-composite-service/build/reports/tests/test/index.html

####  책에 나온 @ExceptionHandler 는 거의 동작하지 않는다. 아래와 같이 수정해서 테스트 하였다

- GlobalControllerExceptionHandler.java

```java
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
 private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

 @ExceptionHandler(NotFoundException.class)
 @ResponseBody
 public ResponseEntity<?> handleNotFoundException(Exception ex) {
  return new ResponseEntity<>(ex, HttpStatus.NOT_FOUND);
 }

 @ExceptionHandler(InvalidInputException.class)
 @ResponseBody
 public ResponseEntity<?> handleInvalidInputException(Exception ex) {
  return new ResponseEntity<>(ex, HttpStatus.UNPROCESSABLE_ENTITY);
 }

}

```

- ProductCompositeServiceApplicationTests.java

```java
    @Test
    public void getProductNotFound() {
     client.get()
      .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
//      .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
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
//      .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
      .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }
```
