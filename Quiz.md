# Quiz

## Chapter 2 | 스프링 부트 소개

### 1. @SpringBootApplication 애노테이션이 제공하는 기능은 무엇인가?

- 컴포넌트 검색을 활성화해 애플리케이션 클래스의 패키지와 모든 하위 패키지에서 스프링 컴포넌트와 구성 클래스(@Component)를 검색한다.
- 애플리케이션 클래스 자체를 구성 클래스로 만든다.
- 자동 설정을 활성화해 스프링 부트가 설정 가능한 JAR 파일을 클래스패스classpath에서 자동으로 찾게 한다. 예를 들어, 톰캣이 클래스패스에 있는 경우 스프링 부트는 톰캣을 내장형 웹 서버로 자동 구성한다.

```java
@SpringBootApplication
public class MyApplication {

  public static void main (String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

### 2. REST 서비스 개발을 위한 컴포넌트인 스프링 웹 MVC와 스프링 웹플럭스의 주요 차이점은 무엇인가?

> 논블로킹 HTTP 클라이언트와 서비스의 개발을 지원

- 애노테이션 기반 명령형 방식: 기존 웹 프레임워크인 스프링 웹 MVC와 유사하지만 리액티브 서비스를 지원
- 함수 지향 모델 기반의 라우터 및 핸들러 방식

### 3. 스프링 폭스를 사용해 REST API를 문서화했을 때의 장점은 무엇인가?

> RESTful 서비스 등을 만들고자 API를 개발하는 경우 사용하기 쉽도록 API를 문서화하는 것이 중요한데 \
> 스웨거는 RESTful 서비스 문서를 공개하는 기능을 다수의 주요 API 게이트웨이가 내장하고 있다. \
> 스프링 폭스는 런타임에 스웨거 기반의 API문서를 생성한다.

### 4. 스프링 데이터의 리포지토리가 제공하는 기능은 무엇이며, 가장 단순한 리포지토리 구현은 무엇인가?

> 여러 유형의 데이터베이스에 데이터를 접근하고자 사용한다. \
> 대개 자바 인터페이스로 선언되며, 스프링 데이터가 상황에 따른 구현을 독자적인 규칙에 맞춰서 생성한다.

- CrudRepository는 생성, 읽기, 업데이트, 삭제 작업을 위한 표준 메서드 제공.
- CrudRepository를 이용해 가장 간단히 리포지토리를 정의할 수 있다.

```java
import org.springframework.data.repository.CrudRepository;

public interface ReviewRepository extends CrudRepository<ReviewEntity, ReviewEntityPK> {
  Collection<ReviewEntity> findByProductId(int productId);
}
```

### 5. 스프링 클라우드 스트림의 바인더가 제공하는 기능은 무엇인가?

> 특정 메시징 시스템과 통합 기능을 제공한다. JDBC 드라이버가 특정 데이터베이스를 지원하는 것과 유사

- 메시지: 메시징 시스템과 주고받는 데이터를 설명하는 데이터 구조
- 게시자: 메시징 시스템에 메시지를 보낸다.
- 구독자: 메시징 시트템에서 메시지를 받는다.
- 채널: 메시징 시스템과 통신하는 데 사용한다. 게시자는 출력 채널을 사용하고 구독자는 입력 채널을 사용한다.

### 6. 도커 컴포즈가 제공하는 기능은 무엇인가?

> 단일 커맨드로 여러 컨테이너를 시작 및 중지할 수 있다.

## Chapter 3 | 공조 마이크로서비스 집합 생성

### 1. 스프링 이니셜라이저로 스프링 부트 프로젝트를 생성할 때 사용 가능한 의존성 목록을 보는 커맨드는 무엇인가?

> spirng init --list

### 2. 그래들이 여러 개의 관련 프로젝트를 하나의 커맨드로 빌드하도록 설정하려면 어떻게 해야 하는가?

1. 먼저 settings.gradle 파일을 생성하고 그래들이 빌드할 프로젝트를 입력한다.

    ```gradle
    <!-- settings.gradle -->
    include ':microservices:product-service'
    include ':microservices:review-service'
    include ':microservices:recommendation-service'
    include ':microservices:product-composite-service'
    ```

2. product-service 프로젝트에서 그래들 실행 파일을 복사한다. 복사한 파일은 멀티 프로젝트 빌드에서 재사용한다.

    ```bash
    # 꼭 product일 필요는 없음 그냥 gradle 실행 파일 한개가 필요할 뿐.
    cp -r microserivce/product-service/gradle .
    cp microserivce/product-service/gradlew .
    cp microserivce/product-service/gradlew.bat .
    cp microserivce/product-service/.gitignore .
    ```

3. 이제 각 프로젝트에선 그래들 실행 파일이 필요 없으므로 다음 커맨드로 제거한다.

    ```bash
    find microservices -depth -name "gradle" -exec rm -rfv "{}" \;
    find microservices -depth -name "gradlew*" -exec rm -fv "{}" \;
    ```

4. 이제 하나의 커맨드로 전체 마이크로서비스를 빌드할 수 있다.

    ```bash
    ./gradlew build
    ```

### 3. @PathVariable과 @RequestParam 애노테이션의 사용 목적은 무엇인가?

> @PathVariable,@RequestParam 둘 다 HTTP 요청으로 전달된 값을 매개 변수에 매핑

- @PathVariable : Path 형식의 URL에서 파라미터를 받아올때 사용 ex) <http://site.com/123>

    ```java
    @GetMapping("/{id}")
    public void getService(@PathVariable("id") int id) {}
    ```

- @RequestParam : 쿼리형식의 URL에서 파라미터를 받아올 때 사용 ex) <http://site.com?id=123>

    ```java
    @GetMapping("/")
    public void getService(@RequestParam(value = "id", required = false) int id) {}
    ```

### 4. API 구현 클래스의 비즈니스 로직과 프로토콜별 예외 처리를 분리하려면 어떻게 해야 하는가?

1. 전역 REST 컨트롤러 예외 핸들러를 만든다.

    ```java
    // 유틸클래스
    @RestControllerAdvice
    public class GlobalControllerExceptionHandler {

        @ResponseStatus(UNPROCESSABLE_ENTITY)
        @ExceptionHandler(InvalidInputException.class)
        public @ResponseBody HttpErrorInfo handleInvalidInputException    (ServerHttpRequest request, Exception ex) {
          return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
        }
    }
    ```

    > API 구현 클래스에서 InvalidInputException을 던지면 유틸리티 클래스는 상태 코드가 422(UNPROCESSABLE_ENTITY)인 HTTP 응답으로 바꾼다.

2. API 구현의 예외처리
  > API 구현에선 util 프로젝트의 예외를 사용해 오류 발생을 알린다.
  > 예외는 문제 상황을 알리는 HTTP 상태 코드로 전환돼 REST 클라이언트에게 다시 전달된다.

  ```java
  // ProductServiceImpl.java
  if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
  ```

  ```java
  // ProductCompositeIntegration.java
  catch (HttpClientErrorException ex) {
    switch (ex.getStatusCode()) {

      case UNPROCESSABLE_ENTITY:
        throw new InvalidInputException(getErrorMessage(ex));
        
    }
  }
  ```

### 5. 모키토의 사용 목적은 무엇인가?

> 마이크로서비스의 API를 독립적으로 테스트하기 위해, 의존성을 모의 객체로 대체하기 위함

```java
when(compositeIntegration.getProduct(PRODUCT_ID_OK))
.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
```

## Chapter 4 | 도커를 사용항 마이크로서비스 배포

### 1. 가상머신과 도커 컨테이너의 주요 차이점은 무엇인가?

> 가상머신은 하이퍼바이저를 사용해 운영체제 전체를 실행한다.

> 반면 도커 컨테이너는 리눅스 호스트에서 실행되며, 리눅스 네임스페이스를 이용해서 사용자, 프로세스, 파일시스템, 네트워킹등의 전역 시스켐 리소스를 컨테이너에 분배한다.

> 리눅스 제어 그룹 ( cgroup ) 을 사용해 컨테이너가 사용할 수 있는 CPU와 메모리를 제한한다.

![가상머신 vs 컨테이너](./screen-shot/virtualization-vs-containers_transparent.png)

### 2. 도커가 네임스페이스, cgroup을 사용하는 목적은 무엇인가?

> 하이퍼바이저를 사용해 운영체제 전체를 실행하는 가상머신과 비교하면 컨테이너의 오버헤드는 일부분에 불과하다.

### 3. 컨테이너의 최대 메모리 설정을 무시하고, 허용량을 초과해 메모리를 할당한 자바 애플리케이션은 어떻게 되는가?

> java SE 버전 10이하에서는 도커를 지원하지 않았다.
> 이전에는 자바가 리눅스 cgroup으로 지정한 자원 할당량을 무시했기 때문에, 자바는 컨테이너에 허용된 메모리를 JVM에 할당하는게 아니라 도커 호스트의 전체 메모리를 할당했다.

```bash
echo 'new byte[500_000_000]' | docker run -i --rm -m=1024M open-jdk:12.0.2 jshell -q
# 도커는 메모리의 1/4를 힙에 할당한다. ( => 256M가 힙에 할당되었고, 500M를 요청한 상태이다. )
```

- java SE 10 이후

> Exception java.lang.OutOfMemoryError:Java heap space
> JVM이 컨테이너의 최대 메모리 설정을 준수하므로 해당 동작을 수행하지 못함.

- java SE 9 이전

> 자바 관점에서 보면 작동하는게 맞다.
> 자바는 총 메모리가 16GB라고 생각하고 힙크디는 4GB로 설정되어 있기 때문.
> 그러나 JVM의 메모리 크기는 1GB를 초과하게 되며, 도커는 즉각 컨테이너를 종료하면서 State engine terminated와 같은 알 수 없는 예외를 출력하게 된다.

### 4. 스프링 기반 애플리케이션을 소스 코드 수정없이 도커 컨테이너로 실행하려면 어떻게 해야 하는가?

> 마이크로서비스를 컨테이너에서 실행하더라도, 소스 코드를 변경할 필요는 없으며, 마이크로서비스의 구성만 변경하면 된다.

- product | application.yml

```yml
# 추가
---
spring.profiles: docker
server.port: 8080
```

- -e: 이 옵션으로 컨테이너의 환경 변수를 지정할 수 있다. 앞의 커맨드에선 SPRING_PROFILES_ACTIVE=docker 환경 변수를 사용해 스프링 프로필을 docker로 지정했다.

```bash
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-service
```

### 5. 다음의 도커 컴포즈 코드가 작동하지 않는 이유는 무엇인가?

> 호스트의 포트 8080이 중복되기 때문.
> 컨테이너의 포트는 독립적이기 때문에 중복가능하지만 호스트는 하나이기 때문에 중복될 수 없다.

```yml
review:
  build: microservices/review-service
  ports:
    - "8080:8080"
  environment:
    - SPRING_PROFILES_ACTIVE=docker

product-composite:
  build: microservice/product-composite-service
  ports:
    - "8080:8080" # 8080 포트가 이미 review-service에 할당되어 있기 때문
  environment:
    - SPRING_PROFILES_ACTIVE=docker
```

## Chapter 5 | OpenAPI/스웨거를 사용한 API 문서화

### 1. 스프링 폭스로 RESTful 서비스의 API 문서를 작성할 때의 장점은 무엇인가?

> 스프링 폭스를 사용하면 API를 구현하는 소스 코드와 연동해 API를 문서화할 수 있다.
> 이런 기능은 자바코드와 API 문서의 수명주기가 다르면 시간이 지나면서 쉽게 어긋나기 때문

### 2. 스프링 폭스가 지원하는 API 문서화 사양은 무엇인가?

> OpenAPI
> 2015년에 스마트베어 소프르웨어가 리눅스 재단 산하의 Open API initiative 스웨거 사양을 거부하고 OpenAPI 사양을 만들었다.

### 3. 스프링 폭스 Docket 빈의 사용 목적은 무엇인가?

> 스프링 폭스 구성에 사용된다. ( 버전관리, path 등등)

### 4. 스프링 폭스가 API 문서 작성을 위해 런타임에 검사하는 애노테이션에는 어떤것들이 있는가?

- @Api - 패키지의 API 정보 ( deprecated )
- @ApiOperation - 해당 서비스의 API 정보
- @ApiResponse - 응답에 대한 설명
- @GetMapping - 스프링 폭스는 @GetMapping 어노테이션을 검사해 오퍼레이션의 파라미터와 응답 유형을 파악

### 5. YAML 파일에서 : ㅣ 의 의미는 무엇인가?

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

### 6. 내장된 스웨거 뷰어로 수행했던 API 호출을 뷰어를 사용하지 않고 반복하려면 어떻게 해야 하는가?

> 스웨거 UI를 사용하지 않고 API 호출을 시도하고 싶다면 응답 항목에서 curl 커맨드를 복사해 터미널 창에서 실행

```bash
curl -X GET "http://localhost:8080/product-composite/123" -H "accept: application/json"
```


