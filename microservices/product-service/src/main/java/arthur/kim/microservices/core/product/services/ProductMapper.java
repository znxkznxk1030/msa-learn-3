package arthur.kim.microservices.core.product.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import arthur.kim.api.product.Product;
import arthur.kim.microservices.core.product.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mappings({
		@Mapping(target = "serviceAddress", ignore = true)
	})
	Product entityToApi(ProductEntity entity);
	
	@Mappings({
		@Mapping(target = "id", ignore = true),
		@Mapping(target = "version", ignore = true)
	})
	ProductEntity apiToEntity(Product api);
}
