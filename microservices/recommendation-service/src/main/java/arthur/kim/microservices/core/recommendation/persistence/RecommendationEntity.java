package arthur.kim.microservices.core.recommendation.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId': 1}")
public class RecommendationEntity {
	@Id
	private String id;
	
	@Version
	private Integer version;
	
	private int productId;
	private int recommenctionId;
	private String author;
	private int rating;
	private String content;
	
	public RecommendationEntity(String id, Integer version, int productId, int recommenctionId, String author,
			int rating, String content) {
		this.productId = productId;
		this.recommenctionId = recommenctionId;
		this.author = author;
		this.rating = rating;
		this.content = content;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getRecommenctionId() {
		return recommenctionId;
	}
	public void setRecommenctionId(int recommenctionId) {
		this.recommenctionId = recommenctionId;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}