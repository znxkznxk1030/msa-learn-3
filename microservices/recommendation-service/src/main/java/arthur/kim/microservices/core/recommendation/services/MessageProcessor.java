package arthur.kim.microservices.core.recommendation.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import arthur.kim.api.event.Event;
import arthur.kim.api.recommendation.Recommendation;
import arthur.kim.api.recommendation.RecommendationService;
import arthur.kim.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

  private final RecommendationService recommendationService;

  @Autowired
  public MessageProcessor(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @StreamListener(target = Sink.INPUT)
  public void process(Event<Integer, Recommendation> event) {

    switch (event.getEventType()) {
      case CREATE:
        Recommendation recommendation = event.getData();
        recommendationService.createRecommendation(recommendation);
        break;
      case DELETE:
        int productId = event.getKey();
        recommendationService.deleteRecommendations(productId);
        break;
      default:
        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
        throw new EventProcessingException(errorMessage);
    }
    
  }
  
}
