package arthur.kim.api.product;

public class Product {
    private final int productId;
    private final String name;
    private final int weight;
    private final String serviceAddress;
    
    public Product() {
    	this.productId = -1;
    	this.name = null;
    	this.serviceAddress = null;
		this.weight = 0;
    }

    public Product(int productId, String name, int weight, String serviceAddress) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.serviceAddress = serviceAddress;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}
