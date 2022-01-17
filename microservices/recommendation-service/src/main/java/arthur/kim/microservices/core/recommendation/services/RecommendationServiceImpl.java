package arthur.kim.microservices.core.recommendation.services;

import java.util.List;

import com.mongodb.DuplicateKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import arthur.kim.api.recommendation.Recommendation;
import arthur.kim.api.recommendation.RecommendationService;
import arthur.kim.microservices.core.recommendation.persistence.RecommendationEntity;
import arthur.kim.microservices.core.recommendation.persistence.RecommendationRepository;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final RecommendationMapper mapper;

  private final RecommendationRepository repository;

  @Autowired
  public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationMapper mapper,
    RecommendationRepository repository) {
    this.serviceUtil = serviceUtil;
    this.mapper = mapper;
    this.repository = repository;
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    if (productId < 1)
      throw new InvalidInputException("Invalid productId: " + productId);

    return repository.findByProductId(productId)
        .log()
        .map(e -> mapper.entityToApi(e))
        .map(e -> {
          e.setServiceAddress(serviceUtil.getServiceAddress());
          return e;
        });
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    if (body.getProductId() < 1)
      throw new InvalidInputException("Invalid productId: " + body.getProductId());

    RecommendationEntity entity = mapper.apiToEntity(body);
    Mono<Recommendation> newEntity = repository.save(entity)
        .log()
        .onErrorMap(
            DuplicateKeyException.class,
            ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:"
                + body.getRecommendationId()))
        .map(e -> mapper.entityToApi(e));

    return newEntity.block();
  }

  @Override
  public void deleteRecommendations(int productId) {
    if (productId < 1)
      throw new InvalidInputException("Invalid productId: " + productId);

    LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId)).block();
  }
}
