package repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import model.Product;

@Repository
public interface BookRepository extends MongoRepository<Product, String> {
	 Page<Product> findByCategoryId(String categoryId, Pageable pageable); // Lấy sách theo categoryId
	    long countByCategoryId(String categoryId); // Đếm số sách theo categoryId
	    
	    
	    
	    Page<Product> findByProductNameNoEllipsisContainingIgnoreCase(String keyword, Pageable pageable);
	    long countByProductNameNoEllipsisContainingIgnoreCase(String keyword);
	    
	 // Tìm sách mà genres chứa genreId
	    Page<Product> findByGenresContaining(String genreId, Pageable pageable);

	    long countByGenresContaining(String genreId);
}
