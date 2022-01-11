package arthur.kim.microservices.core.product.composite;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arthur.kim.api.event.Event;

public class IsSameEvent extends TypeSafeMatcher<String> {

  private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

  private ObjectMapper mapper = new ObjectMapper();

  private Event expectedEvent;

  private IsSameEvent(Event expectedEvent) {
    this.expectedEvent = expectedEvent;
  }

  @Override
  protected boolean matchesSafely(String item) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void describeTo(Description description) {
    // TODO Auto-generated method stub

  }
  
}
