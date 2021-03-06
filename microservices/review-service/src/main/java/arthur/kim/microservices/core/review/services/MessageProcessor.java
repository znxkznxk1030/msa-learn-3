package arthur.kim.microservices.core.review.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import arthur.kim.api.event.Event;
import arthur.kim.api.review.Review;
import arthur.kim.api.review.ReviewService;
import arthur.kim.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

  private final ReviewService reviewService;

  @Autowired
  public MessageProcessor(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @StreamListener(target = Sink.INPUT)
  public void process(Event<Integer, Review> event) {

    switch (event.getEventType()) {
      case CREATE:
        Review review = event.getData();
        reviewService.createReview(review);
        break;
      case DELETE:
        int productId = event.getKey();
        reviewService.deleteReviews(productId);
        break;
      default:
        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
        throw new EventProcessingException(errorMessage);
    }
    
  }
  
}
