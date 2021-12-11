# Hands-On Microservices with Spring Boot And Spring Cloud

## Chapter 3

### gradle 설정

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
      .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }
```

### bash script로 통합 테스트 하기

#### assertCurl()

``` bash
function assertCurl() {
  
  local expectedHttpCode=$1
  local curlCmd="$2 -w\"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
      echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo "- Failing command: $curlCmd"
      echo "- Response Body: $RESPONSE"
      exit 1
  fi
}
```

#### assertEqual()

``` bash
function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}
```

#### curl | -w 옵션

> -w, --write-out FORMAT <format>

- 응답에서 포맷에 맞는 데이터를 출력할 수 있다.
- %{variable_name} 과 같은 식으로 출력할 수 있다.
- content_type, http_code, time_total 등의 정보를 출력할 수 있다.

#### curl | -s 옵션

> -s, --silent

- 프로그레스나 에러 정보를 보여주지 않는다.

#### local

> 선언된 변수는 기본적으로 전역 변수(global variable)다. 단 함수 안에서만 지역 변수(local variable)를 사용할 수 있는데 사용할려면 변수 명 앞에 local을 붙여주면 된다.

#### ${#변수}

> 문자열 길이(예: echo ${#string})

#### ${변수%단어}

> 변수의 뒷부분부터 짧게 일치한 단어 삭제(예: echo ${string%b*c})

``` bash
string="abc-efg-123-abc"
echo ${string%b*c} # abc-efg-123-a
echo ${string%???} # abc-efg-123-
```

#### ./bash-test.bash: Permission denied

``` bash
chmod u+x ./bash-test.bash 
# u stands for user.
# g stands for group.
# o stands for others.
# a stands for all. ( default )
# chmod +x {filename} | you'll make it executable.
```

#### curl | 결과값

> curl <http://localhost:7000/product-composite/1> -s -w "%{http_code}"

```json
{
  "productId": 1,
  "name": "name-1",
  "weight": 123,
  "recommendations": [
    {
      "recommendationId": 1,
      "author": "Author 1",
      "rate": 1
    },
    {
      "recommendationId": 2,
      "author": "Author 2",
      "rate": 2
    },
    {
      "recommendationId": 3,
      "author": "Author 3",
      "rate": 3
    }
  ],
  "reviews": [
    {
      "reviewId": 1,
      "author": "Author 1",
      "subject": "Subject 1"
    },
    {
      "reviewId": 2,
      "author": "Author 2",
      "subject": "Subject 2"
    },
    {
      "reviewId": 3,
      "author": "Author 3",
      "subject": "Subject 3"
    }
  ],
  "serviceAddresses": {
    "cmp": "yskimui-MacBook-Pro.local/218.38.137.27:7000",
    "pro": "yskimui-MacBook-Pro.local/218.38.137.27:7001",
    "rev": "yskimui-MacBook-Pro.local/218.38.137.27:7003",
    "rec": "yskimui-MacBook-Pro.local/218.38.137.27:7002"
  }
}
200
```

## Chapter 4

### 첫 도커 명령 실행

```bash
docker run -it --rm ubuntu
```

#### -it

- 터미널을 통해 컨테이너와 상호작용

#### --rm

- 테미널 세션을 마치면 종료하도록 설정

### 컨테이너 모두 제거

```bash
docker rm -f $(docker ps -aq)
```

#### -a

- 실행 중 이거나, 중지된 컨테이너의 ID를 출력

#### -q

- 커맨드의 출력을 정리하고 컨테이너 ID만 남긴다.

### 도커에서 자바를 실행할 때의 문제

- 9 버전 이하의 자바에서는 리눅스 cgroup으로 지정한 자원 할당량을 무시했었음

### 도커 이미지 빌드

#### jar으로 프로젝트 빌드

```bash
./gradlew :microservices/product-service/build
```

#### Dockerfile을 이용해 도커 이미지 빌드

```Dockerfile
FROM openjdk:12.0.2

EXPOSE 8080

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t product-service .

# 확인
docker images | grep product-service
```

#### -t ( TAG 이름 )

#### 도커 이미지 실행

```bash
docker run --rm -p 8080:8080 -e "SPRING_PROFILE_ACTIVE=docker" product-service
```

#### 실행 확인

```bash
curl localhost:8080/product/3 | jq
```

#### 데몬모드로 실행하기

```bash
docker run -d -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name my-prd-svc product-service
```

- 로그 보기

```bash
docker logs my-prd-srv -f
```

### docker-compose.yml

#### 마이크로서비스의 이름 => 도커 내부 네크워크에서 사용하는 컨테이너의 호스트 이름

#### build : Dockerfile의 위치를 지정하는 빌드 지시문

#### mem_limit : 메모리는 350MB로 제한, 이래야 총 6GB로 맞출 수 있음

#### environment : 환경변수, 스프링 프로필 지정하기 위함

```yml
version: '2.1'

services:
   product:
      build: microservices/product-service
      mem_limit: 350m
      environment:
         - SPRING_PROFILES_ACTIVE=docker
         
   recommendation:
      build: microservices/recommendation-service
      mem_limit: 350m
      environment:
         - SPRING_PROFILES_ACTIVE=docker

   reivew:
      build: microservices/reivew-service
      mem_limit: 350m
      environment:
         - SPRING_PROFILES_ACTIVE=docker

   product-composite:
      build: microservices/product-composite-service
      mem_limit: 350m
      ports:
         - "8080:8080"
      environment:
         - SPRING_PROFILES_ACTIVE=docker
```

### 마이크로서비스 환경 시작

#### 이미지 빌드

```bash
./gradlew build
docker-compose build
```

![docker-compose build](./screen-shot/docker-compose_build.png)

#### 이미지 확인

```bash
docker images
```

![docker images](screen-shot/docker_images.png)

#### 도커 실행

```bash
docker-compose up -d
```

#### 모니터링

```bash
docker-compose logs -f
```

![docker-compose logs](./screen-shot/docker-compose_logs.png)

#### 테스트

```bash
curl localhost:8080/product-composite/123 -s | jq .
```

![docker-compose test](./screen-shot/docker-compose_test.png)

#### 종료

```bash
docker-compose down
```

### bash script를 이용한 마이크로 서비스 환경테스트

#### testUrl

```bash
function testUrl() {
  url=$@
  if curl $url -ks -f -o /dev/null
  then
    echo "Ok"
    return 0
  else
    echo -n "not yet"
    return 1
  fi;
}
```

#### waitForService

```bash
function waitForService() {
  url=$@
  echo -n "Wait for: $url..."
  n=0
  until testUrl $url
  do
    n=$((n + 1))
    if [[ $n == 100 ]]
    then
      echo " Give up "
      exit 1
    else
      sleep 6
      echo -n ", retry #$n "
    fi
  done
}
```

## Chapter 5 | OpenAPI / 스웨거를 사용한 API 문서화

### 목표 : product-composite-service 노출하는 공개 API에 스퀘거 기반 문서추가

### 의존성 추가

- springfox-swagger2 : 스웨거 2 기반의 문서 생성
- springfox-spring-webflux : 스프링 웹플럭스 기반의 RESTful 오퍼레이션 지원
- springfox-swagger-ui : 마이크로서비스에 스웨거 뷰어를 내장

```gradle
<!-- product-composite-service -->
implementation "io.springfox:springfox-boot-starter:3.0.0"
```

```gradle
<!-- api -->
implementation "io.springfox:springfox-boot-starter:3.0.0"
```

### SpringFoxConfig

```java
@Value("${api.common.version}")
 String apiVersion;
 @Value("${api.common.title}")
 String apiTitle;
 @Value("${api.common.description}")
 String apiDescription;
 @Value("${api.common.termsOfServiceUrl}")
 String apiTermsOfServiceUrl;
 @Value("${api.common.license}")
 String apiLicense;
 @Value("${api.common.licenseUrl}")
 String apiLicenseUrl;
 @Value("${api.common.contact.name}")
 String apiContactName;
 @Value("${api.common.contact.url}")
 String apiContactUrl;
 @Value("${api.common.contact.email}")
 String apiContactEmail;

 @Bean
 public Docket apiDocumentation() {
  return new Docket(DocumentationType.SWAGGER_2)
    .select()
    .apis(RequestHandlerSelectors.any())
    .paths(PathSelectors.any()).build()
    .apiInfo(new ApiInfo(apiTitle, apiDescription, apiVersion, apiTermsOfServiceUrl,
      new Contact(apiContactName, apiContactUrl, apiContactEmail), apiLicense, apiLicenseUrl,
      emptyList()));
 }
```

- 스웨거 V2 문서를 생성하고자 Docket 빈을 초기화한다.
- apis(), paths() 메서드로 스프링 폭스가 API 정보를 찾을 위치를 지정

### Docket

- API 문서는 버전관리가 되어야 하므로, Docket Bean 단위로 버전관리를 작성

#### select()

- ApiSelectorBuilder를 생성

#### apis()

- api 스펙이 작성되어 있는 패키지 지정

#### paths()

- apis()로 선택되어진 API중에서 특정 path 조건에 맞는 API들을 다시 필터링 하여 문서화

#### apiInfo()

- 제목, 설명 등 문서에 대한 정보들을 보여주기 위해 호출

```java
public ApiInfo( title, description, version, termsOfServiceUrl, contact, license, licenseUrl, vendorExtensions )
```

### 문서 작성

```java
@Api(description = "REST API for composite product information")
public interface ProductCompositeService {

 /**
  * Sample usage: curl $HOST:$PORT/product-composite/1
  *
  * @param productId
  * @return the composite product info, if found, else null
  */
 @ApiOperation(value = "${api.product-composite.get-composite-product.description}", notes = "${api.product-composite.get-composite-product.notes}")
 @ApiResponses(value = {
   @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
   @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
   @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.") })
 @GetMapping(value = "/product-composite/{productId}", produces = "application/json")
 ProductAggregate getProduct(@PathVariable int productId);
}

```

### 명세서 ( application.yml )

```yml
api:

  common:
    version: 1.0.0
    title: Sample API
    description: Description of the API...
    termsOfServiceUrl: MINE TERMS OF SERVICE URL
    license: License
    licenseUrl: MY LICENSE URL

    contact:
      name: Contact
      url: My
      email: me@mail.com

  product-composite:
    get-composite-product:
      description: Returns a composite view of the specified product id
      notes: |
        # Normal response
        If the requested product id is found the method will return information regarding:
        1. Base product information
        2. Reviews
        3. Recommendations
        4. Service Addresses\n(technical information regarding the addresses of the microservices that created the response)

        # Expected partial and error responses
        In the following cases, only a partial response be created (used to simplify testing of error conditions)

        ## Product id 113
        200 - Ok, but no recommendations will be returned

        ## Product id 213
        200 - Ok, but no reviews will be returned

        ## Non numerical product id
        400 - A <b>Bad Request</b> error will be returned

        ## Product id 13
        404 - A <b>Not Found</b> error will be returned

        ## Negative product ids
        422 - An <b>Unprocessable Entity</b> error will be returned
```
  
### swagger-ui  이슈

> 스프링 - 2.3.x, swagger - 3.0.0 버전에서는 페이지가 안뜨는 이슈가 있다.

<https://stackoverflow.com/questions/48311447/springboot-swagger-url-shows-whitelabel-error-page>

### swagger-ui  접속

> <http://localhost:8080/swagger-ui/index.html>

![swagger-ui](./screen-shot/swagger-ui.png)
![swagger-ui2](./screen-shot/swagger-ui2.png)

### Q&A

#### 1. 스프링 폭스로 RESTful 서비스의 API 문서를 작성할 때의 장점은 무엇인가?

> 스프링 폭스를 사용하면 API를 구현하는 소스 코드와 연동해 API를 문서화할 수 있다.
> 이런 기능은 자바코드와 API 문서의 수명주기가 다르면 시간이 지나면서 쉽게 어긋나기 때문

#### 2. 스프링 폭스가 지원하는 API 문서화 사양은 무엇인가?

> OpenAPI
> 2015년에 스마트베어 소프르웨어가 리눅스 재단 산하의 Open API initiative 스웨거 사양을 거부하고 OpenAPI 사양을 만들었다.

#### 3. 스프링 폭스 Docket 빈의 사용 목적은 무엇인가?

> 스프링 폭스 구성에 사용된다. ( 버전관리, path 등등)

#### 4. 스프링 폭스가 API 문서 작성을 위해 런타임에 검사하는 애노테이션에는 어떤것들이 있는가?

- @Api - 패키지의 API 정보 ( deprecated )
- @ApiOperation - 해당 서비스의 API 정보
- @ApiResponse - 응답에 대한 설명
- @GetMapping - 스프링 폭스는 @GetMapping 어노테이션을 검사해 오퍼레이션의 파라미터와 응답 유형을 파악

#### 5. YAML 파일에서 : ㅣ 의 의미는 무엇인가?

- ":" 는 통상 key 가 된다

```yml
key: value

key: 
  key1:
    key2:
```

- "|" 는 줄바꿈을 포함, ">"는 줄바꿈을 무시

```yml
line_break: | 
  hello,
  world

single_line: >
  hello,
  world
```

#### 6. 내장된 스웨거 뷰어로 수행했던 API 호출을 뷰얼르 사용하지 않고 반복하려면 어떻게 해야 하는가?

> 스웨거 UI를 사용하지 않고 API 호출을 시도하고 싶다면 응답 항목에서 curl 커맨드를 복사해 터미널 창에서 실행

```bash
curl -X GET "http://localhost:8080/product-composite/123" -H "accept: application/json"
```