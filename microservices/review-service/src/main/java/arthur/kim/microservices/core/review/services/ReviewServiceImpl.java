package arthur.kim.microservices.core.review.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import arthur.kim.api.review.Review;
import arthur.kim.api.review.ReviewService;
import arthur.kim.microservices.core.review.persistence.ReviewEntity;
import arthur.kim.microservices.core.review.persistence.ReviewRepository;
import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@RestController
public class ReviewServiceImpl implements ReviewService {
	
	private final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
	
	private final ServiceUtil serviceUtil;

  private final ReviewMapper mapper;

  private final ReviewRepository repository;

  private final Scheduler scheduler;
	
	@Autowired
	public ReviewServiceImpl(Scheduler scheduler, ServiceUtil serviceUtil, ReviewMapper mapper, ReviewRepository repository) {
    this.scheduler = scheduler;
	this.serviceUtil = serviceUtil;
    this.mapper = mapper;
    this.repository = repository;
	}

	@Override
	public Flux<Review> getReviews(int productId) {
		
		if ( productId < 1 ) throw new InvalidInputException("Invalid productId: " + productId);
		
		return asyncFlux(getByProductId(productId)).log();
	}
	
	protected List<Review> getByProductId(int productId) {
		List<ReviewEntity> entityList = repository.findByProductId(productId);
		List<Review> list = mapper.entityListToApiList(entityList);
		list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
		
		return list;
	}
	
	private <T> Flux<T> asyncFlux(Iterable<T> iterable) {
		return Flux.fromIterable(iterable).publishOn(scheduler);
	}

	@Override
	public Review createReview(Review body) {
		try {
      ReviewEntity entity = mapper.apiToEntity(body);
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
      return mapper.entityToApi(newEntity);
    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id: " + body.getReviewId());
    }
	}

	@Override
	public void deleteReviews(int productId) {
    LOG.debug("deleteReviews: tries to delete reveiws for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
	}

}
