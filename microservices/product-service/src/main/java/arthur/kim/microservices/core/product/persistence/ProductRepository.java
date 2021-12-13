package arthur.kim.microservices.core.product.persistence;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String>{

	Optional<ProductEntity> findByProductId(int productId);
}
