package arthur.kim.util.reactor;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReactorTests {

  @Test
  public void TestFlux() {
    List<Integer> list = new ArrayList<>();

    Flux<Integer> subs = Flux.just(1, 2, 3, 4)
        .filter(n -> n % 2 == 0)
        .map(n -> n * 2)
        .log();

    subs.subscribe(n -> list.add(n));

    assertThat(list, contains(4, 8));
  }

  @Test
  public void TestFluxBlocking() {

    List<Integer> list = Flux.just(1, 2, 3, 4)
        .filter(n -> n % 2 == 0)
        .map(n -> n * 2).log()
        .collectList()
        .block();

    assertThat(list, contains(4, 8));
  }

}
