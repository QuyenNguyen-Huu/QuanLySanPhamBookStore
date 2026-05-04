package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dao.BookDao;
import model.Category;
import model.Genre;
import model.Product;
import repository.CategoryRepository;
import repository.GenreRepository;

@Controller
public class HomeController {
	
	@Autowired
    private BookDao bookDao;
	
	  @Autowired
	private	CategoryRepository categoryDao;
	
	  @Autowired
	  private GenreRepository genreDao;
	
	  @RequestMapping("/home")
	  public String test(Model model,
	                     @RequestParam(defaultValue = "0") int page,
	                     @RequestParam(defaultValue = "8") int size,
	                     @RequestParam(required = false) String categoryId,
	                     @RequestParam(required = false) String keyword,
	                     @RequestParam(required = false) String genreId,
	                     HttpSession session) {
	      
	      List<Product> books;
	      long totalBooks;

	      if (keyword != null && !keyword.isEmpty()) {
	    	    // Tìm sách theo keyword
	    	    books = bookDao.searchBooks(keyword, page, size);
	    	    totalBooks = bookDao.countBooksByKeyword(keyword);
	    	} else if (categoryId != null && !categoryId.isEmpty()) {
	    	    // Lấy sách theo categoryId
	    	    books = bookDao.getBooksByCategory(categoryId, page, size);
	    	    totalBooks = bookDao.countBooksByCategory(categoryId);
	    	} else if (genreId != null && !genreId.isEmpty()) {
	    	    // Lấy sách theo genreId (nếu có)
	    	    books = bookDao.getBooksByGenre(genreId, page, size);
	    	    totalBooks = bookDao.countBooksByGenre(genreId);
	    	} else {
	    	    // Lấy tất cả sách
	    	    books = bookDao.getBooksByPage(page, size);
	    	    totalBooks = bookDao.countBooks();
	    	}

	      int totalPages = (int) Math.ceil((double) totalBooks / size);
	      
	      List<Category> categories = categoryDao.findAll();
	      List<Genre> genres = genreDao.findAll();
	      
	   // Tạo Map từ genreId sang genreName để lookup
	      Map<String, String> genreIdToName = genres.stream()
	              .collect(Collectors.toMap(Genre::getId, Genre::getName));

	      // Tạo Map từ productId sang List<String> tên genres
	      Map<String, List<String>> productGenresMap = new HashMap<>();
	      for (Product p : books) {
	          List<String> genreNames = p.getGenres().stream()
	                  .map(genreIdToName::get)
	                  .filter(name -> name != null)
	                  .collect(Collectors.toList());
	          productGenresMap.put(p.getId(), genreNames);
	      }

	      // Lấy top 5 sách giảm giá nhiều nhất
	      List<Product> topDiscountedBooks = bookDao.getTopDiscountedBooks(20);
	      List<Product> suggestedBooks = bookDao.getTopDiscountedBooks(10); // lấy 10 quyển gợi ý
	      model.addAttribute("suggestedBooks", suggestedBooks);
	      

	      model.addAttribute("products", books);
	      model.addAttribute("total", totalBooks);
	      model.addAttribute("currentPage", page);
	      model.addAttribute("totalPages", totalPages);
	      model.addAttribute("categories", categories);
	      model.addAttribute("genres", genres);
	      model.addAttribute("selectedCategoryId", categoryId);
	      model.addAttribute("selectedGenreId", genreId);
	      model.addAttribute("keyword", keyword);
	      model.addAttribute("topDiscountedBooks", topDiscountedBooks);
	      
	   // Thêm map genre names để hiển thị trong view
	      model.addAttribute("productGenresMap", productGenresMap);

	      return "home";
	  }
	

}
