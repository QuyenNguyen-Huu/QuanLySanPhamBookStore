package dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Product;
import repository.BookRepository;

@Service
public class BookDao {
	@Autowired
    private BookRepository bookRepository;


	    // Lấy tất cả sách
	    public List<Product> getAllBooks() {
	        return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	    }

	    // Lấy sách theo phân trang
	    public List<Product> getBooksByPage(int page, int size) {
	        return bookRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
	                             .getContent();
	    }

	    // Đếm tổng số sách
	    public long countBooks() {
	        return bookRepository.count();
	    }
	    
	    // Lấy sách theo categoryId và phân trang
	    public List<Product> getBooksByCategory(String categoryId, int page, int size) {
	        return bookRepository.findByCategoryId(categoryId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
	                             .getContent();
	    }

	    // Đếm số sách theo categoryId
	    public long countBooksByCategory(String categoryId) {
	        return bookRepository.countByCategoryId(categoryId);
	    }
	    
	    public List<Product> getTopDiscountedBooks(int limit) {
	        List<Product> allBooks = bookRepository.findAll();
	        // Sắp xếp giảm dần theo discountPercent (nếu null thì 0)
	        allBooks.sort((a, b) -> {
	            int da = a.getDiscountPercent() != null ? a.getDiscountPercent() : 0;
	            int db = b.getDiscountPercent() != null ? b.getDiscountPercent() : 0;
	            return Integer.compare(db, da);
	        });
	        return allBooks.stream().limit(limit).toList();
	    }
	    
	 // Tìm sách theo keyword với phân trang
	    public List<Product> searchBooks(String keyword, int page, int size) {
	        return bookRepository.findByProductNameNoEllipsisContainingIgnoreCase(keyword, 
	            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))).getContent();
	    }

	    // Đếm số sách theo keyword
	    public long countBooksByKeyword(String keyword) {
	        return bookRepository.countByProductNameNoEllipsisContainingIgnoreCase(keyword);
	    }
	    
	 // Lấy sách theo genreId và phân trang
	    public List<Product> getBooksByGenre(String genreId, int page, int size) {
	        return bookRepository.findByGenresContaining(genreId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
	                             .getContent();
	    }
	    // Đếm số sách theo genreId
	    public long countBooksByGenre(String genreId) {
	        return bookRepository.countByGenresContaining(genreId);
	    }
}
