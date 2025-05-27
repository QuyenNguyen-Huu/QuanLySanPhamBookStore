package model;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "books")
public class Product {
	@Id
    private String id;
	@NotBlank(message = "Tên sản phẩm không được để trống")
	@Field("product-name-no-ellipsis")
    private String productNameNoEllipsis;
	@Field("lazyloaded-src")
    private String lazyloadedSrc;
	@Field("product-image-href")
    private String productImageHref;
   
	@Field("price")
    private String price;
	@Field("price-2")
    private String price2;
	
	@NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
	@Field("quantity")
    private Integer quantity;
	
	@Field("description")
	private String description;
	
	
	public List<String> getImagePaths() {
		return imagePaths;
	}

	public void setImagePaths(List<String> imagePaths) {
		this.imagePaths = imagePaths;
	}

	private List<String> imagePaths;
	
	private Integer discountPercent;

	public Integer getDiscountPercent() {
	    try {
	        // Xóa dấu chấm trong chuỗi
	    	String p1Str = this.price.replace(".", "").replace("đ", "").trim();
	        String p2Str = this.price2.replace(".", "").replace("đ", "").trim();

	        // Ép sang Integer
	        int p1 = Integer.parseInt(p1Str);
	        int p2 = Integer.parseInt(p2Str);

	        if (p2 > p1) {
	            return (p2 - p1) * 100 / p1;
	        }
	    } catch (NumberFormatException | NullPointerException e) {
	        return 0;
	    }
	    return 0;
	}

	public void setDiscountPercent(Integer discountPercent) {
	    this.discountPercent = discountPercent;
	}

	public String getDescription() {
	    return description;
	}

	public void setDescription(String description) {
	    this.description = description;
	}
	
	private String categoryId;
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	private List<String> genres;
    
	public String getId() {
		return id;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public List<String> getGenres() {
		return genres;
	}
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProductNameNoEllipsis() {
		return productNameNoEllipsis;
	}
	public void setProductNameNoEllipsis(String productNameNoEllipsis) {
		this.productNameNoEllipsis = productNameNoEllipsis;
	}
	public String getLazyloadedSrc() {
		return lazyloadedSrc;
	}
	public void setLazyloadedSrc(String lazyloadedSrc) {
		this.lazyloadedSrc = lazyloadedSrc;
	}
	public String getProductImageHref() {
		return productImageHref;
	}
	public void setProductImageHref(String productImageHref) {
		this.productImageHref = productImageHref;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getPrice2() {
		return price2;
	}
	public void setPrice2(String price2) {
		this.price2 = price2;
	}
    
    
}
