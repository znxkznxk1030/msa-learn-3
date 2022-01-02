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

  // @Override
  // public Recommendation createRecommendation(Recommendation body) {
  //   try {
  //     RecommendationEntity entity = mapper.apiToEntity(body);
  //     RecommendationEntity newEntity = repository.save(entity);

  //     LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(),
  //         body.getRecommendationId());

  //     return mapper.entityToApi(newEntity);
  //   } catch (DuplicateKeyException dke) {
  //     throw new InvalidInputException(
  //         "Duplicate Key, Product Id: " + body.getProductId() + ", Recommendation Id: " + body.getRecommendationId());
  //   } catch (Exception e) {
  //     throw new InvalidInputException(
  //         "Duplicate Key, Product Id: " + body.getProductId() + ", Recommendation Id: " + body.getRecommendationId());
  //   }
  // }

  // @Override
  // public void deleteRecommendations(int productId) {
  //   LOG.debug("deleteRecommendation: tried to delete recommendations for the product with productId: {}", productId);
  //   repository.deleteAll(repository.findByProductId(productId));
  // }
}
