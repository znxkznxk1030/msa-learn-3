package arthur.kim.microservices.core.review.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer>{
	@Transactional(readOnly = true) // 읽기전용이므로 영속성 컨택스트는 스냅샷을 보관하지 않는다. => 메모리 사용량을 최적화 시킬 수 있다.
	List<ReviewEntity> findByProductId(int productId);

}
