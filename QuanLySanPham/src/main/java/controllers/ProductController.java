package controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dao.BookDao;
import model.Category;
import model.Genre;
import model.Product;
import repository.BookRepository;
import repository.CategoryRepository;
import repository.GenreRepository;

@Controller
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private BookDao bookDao;

	@Autowired
	private BookRepository bookRepo;

	@Autowired
	private CategoryRepository categoryRepo;

	@Autowired
	private GenreRepository genreRepo;

	@Autowired
	private ServletContext servletContext;

	private void loadCategoriesAndGenres(Model model) {
		model.addAttribute("categories", categoryRepo.findAll());
		model.addAttribute("genres", genreRepo.findAll());
	}

	private String getUploadDir() {
		return servletContext.getInitParameter("uploadDir");
	}
	
	private String convertToBase64(MultipartFile file) throws IOException {
	    String contentType = file.getContentType(); // ví dụ: image/jpeg, image/png
	    if (contentType == null) contentType = "image/jpeg"; // fallback

	    byte[] bytes = file.getBytes();
	    String base64 = Base64.getEncoder().encodeToString(bytes);

	    return "data:" + contentType + ";base64," + base64.replaceAll("\\s", "");
	}

	private List<String> saveImagesAsBase64(MultipartFile[] files) throws Exception {
	    List<String> base64Images = new ArrayList<>();

	    for (MultipartFile file : files) {
	        if (file.isEmpty()) continue;

	        String base64 = convertToBase64(file);

	        // Fix prefix nếu thiếu
	        if (!base64.startsWith("data:image")) {
	            String contentType = file.getContentType();
	            if (contentType == null) contentType = "image/jpeg";
	            base64 = "data:" + contentType + ";base64," + base64;
	        }

	        base64Images.add(base64);
	    }

	    return base64Images;
	}

	private void deleteImage(String imagePath) {
		if (imagePath != null && !imagePath.isEmpty()) {
			// Nếu là base64 thì bỏ qua
			if (imagePath.startsWith("data:image")) {
				System.out.println("Ảnh base64, không cần xoá file.");
				return;
			}

			// Nếu là đường dẫn thì tiếp tục xoá
			String fileName = imagePath.replace("/uploads/", "");
			String uploadDir = getUploadDir();

			if (uploadDir == null) {
				System.err.println("Thư mục upload null, không thể xoá ảnh.");
				return;
			}

			Path pathToDelete = Paths.get(uploadDir, fileName);
			try {
				Files.deleteIfExists(pathToDelete);
				System.out.println("Đã xoá ảnh file: " + pathToDelete);
			} catch (Exception e) {
				System.err.println("Không thể xoá ảnh cũ: " + e.getMessage());
			}
		}
	}

	// Hiển thị form thêm sản phẩm
	@GetMapping("/add")
	public String showAddForm(HttpSession session, Model model) {
		if (!isLoggedIn(session))
			return "redirect:/login";

		model.addAttribute("product", new Product());
		loadCategoriesAndGenres(model);
		return "product_add";
	}

	// Xử lý thêm sản phẩm
	@PostMapping("/add")
	public String addProduct(@Validated @ModelAttribute("product") Product book, BindingResult result,
			@RequestParam("imageFiles") MultipartFile[] files, Model model) {

		validatePriceFields(book, result);

		if (result.hasErrors()) {
			loadCategoriesAndGenres(model);
			return "product_add";
		}

		try {
	        List<String> base64Images = saveImagesAsBase64(files);

	        if (!base64Images.isEmpty()) {
	            // Ảnh đầu tiên -> lazyloadedSrc
	            book.setLazyloadedSrc(base64Images.get(0));

	            // Các ảnh còn lại -> imagePaths
	            if (base64Images.size() > 1) {
	                book.setImagePaths(base64Images.subList(1, base64Images.size()));
	            } else {
	                book.setImagePaths(new ArrayList<>());
	            }
	        }

			bookRepo.save(book);
			return "redirect:/products/list";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Lỗi khi tải ảnh lên: " + e.getMessage());
			loadCategoriesAndGenres(model);
			return "product_add";
		}
	}

	// Hiển thị danh sách sản phẩm
	@GetMapping("/list")
	public String listProducts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			@RequestParam(value = "category", required = false) String categoryId,
			@RequestParam(value = "keyword", required = false) String keyword, Model model, HttpSession session) {
		if (!isLoggedIn(session))
			return "redirect:/login";

		List<Category> categories = categoryRepo.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("selectedCategory", categoryId);
		model.addAttribute("keyword", keyword);

//  Lọc theo từ khóa nếu có
		List<Product> products;
		long totalBooks;
		if (keyword != null && !keyword.trim().isEmpty()) {
			// Lọc theo keyword
			products = bookDao.searchBooks(keyword.trim(), page, size);
			totalBooks = bookDao.countBooksByKeyword(keyword.trim());
		} else if (categoryId != null && !categoryId.trim().isEmpty()) {
			// Lọc theo categoryId
			products = bookDao.getBooksByCategory(categoryId, page, size);
			totalBooks = bookDao.countBooksByCategory(categoryId);
		} else {
			// Mặc định: tất cả sản phẩm
			products = bookDao.getBooksByPage(page, size);
			totalBooks = bookDao.countBooks();
		}

		int totalPages = (int) Math.ceil((double) totalBooks / size);

		model.addAttribute("products", products);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);

		return "CRUD";
	}

	// Hiển thị form chỉnh sửa sản phẩm
	@GetMapping("/edit")
	public String showEditForm(@RequestParam("id") String id, Model model, HttpSession session) {
		if (!isLoggedIn(session))
			return "redirect:/login";

		Product product = bookRepo.findById(id).orElse(null);
		if (product == null) {
			return "redirect:/products/list";
		}

		model.addAttribute("product", product);
		loadCategoriesAndGenres(model);
		return "product_edit";
	}

	// Xử lý chỉnh sửa sản phẩm
	@PostMapping("/edit/{id}")
	public String editProduct(@Validated @PathVariable("id") String id,
			@ModelAttribute("product") Product updatedProduct, BindingResult result,
			@RequestParam("imageFiles") MultipartFile[] files,
			@RequestParam(value = "keepImages", required = false) List<String> keepImages, Model model,
			HttpSession session) {

		if (!isLoggedIn(session))
			return "redirect:/login";

		validatePriceFields(updatedProduct, result);

		if (result.hasErrors()) {
			loadCategoriesAndGenres(model);
			return "product_edit";
		}

		 try {
		        Product existingProduct = bookRepo.findById(id).orElse(null);
		        if (existingProduct == null) {
		            return "redirect:/products/list";
		        }

		        // 1️⃣ Ảnh giữ lại
		        List<String> finalImageList = new ArrayList<>();
		        String finalLazyloadedSrc = null;

		        if (keepImages != null) {
		            for (String img : keepImages) {
		                if (img.equals(existingProduct.getLazyloadedSrc())) {
		                    finalLazyloadedSrc = img; // Ảnh chính
		                } else {
		                    finalImageList.add(img); // Ảnh phụ
		                }
		            }
		        }

		        // 2️⃣ Ảnh mới (nếu có)
		        if (files.length > 0 && !files[0].isEmpty()) {
		            List<String> newBase64Images = new ArrayList<>();
		            for (MultipartFile file : files) {
		                if (!file.isEmpty()) {
		                    String contentType = file.getContentType();
		                    if (contentType == null) contentType = "image/jpeg";
		                    String base64 = Base64.getEncoder().encodeToString(file.getBytes());
		                    base64 = "data:" + contentType + ";base64," + base64;
		                    newBase64Images.add(base64);
		                }
		            }

		            if (!newBase64Images.isEmpty()) {
		                if (finalLazyloadedSrc == null) {
		                    finalLazyloadedSrc = newBase64Images.get(0);
		                    newBase64Images.remove(0);
		                }
		                finalImageList.addAll(newBase64Images);
		            }
		        }

		        // 3️⃣ Cập nhật thông tin sản phẩm
		        existingProduct.setLazyloadedSrc(finalLazyloadedSrc);
		        existingProduct.setImagePaths(finalImageList);
		        existingProduct.setProductNameNoEllipsis(updatedProduct.getProductNameNoEllipsis());
		        existingProduct.setPrice(updatedProduct.getPrice());
		        existingProduct.setPrice2(updatedProduct.getPrice2());
		        existingProduct.setCategoryId(updatedProduct.getCategoryId());
		        existingProduct.setGenres(updatedProduct.getGenres());

		        bookRepo.save(existingProduct);

		        return "redirect:/products/list";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
			loadCategoriesAndGenres(model);
			return "product_edit";
		}
	}

	// Xử lý xóa sản phẩm
	@PostMapping("/delete")
	public String deleteProduct(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
		try {
			Product product = bookRepo.findById(id).orElse(null);
			if (product != null) {
				// Xoá ảnh nếu có
				deleteImage(product.getLazyloadedSrc());
				if (product.getImagePaths() != null) {
					for (String imagePath : product.getImagePaths()) {
						deleteImage(imagePath);
					}
				}

				// Xoá sản phẩm khỏi MongoDB
				bookRepo.deleteById(id);

				redirectAttributes.addFlashAttribute("success", "Xoá sản phẩm thành công!");
			} else {
				redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm để xoá.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xoá sản phẩm: " + e.getMessage());
		}

		return "redirect:/products/list";
	}

	private void validatePriceFields(Product product, BindingResult result) {
		try {
			double price = Double.parseDouble(product.getPrice());
			if (price <= 0) {
				result.rejectValue("price", "error.price", "Giá phải lớn hơn 0");
			}
		} catch (NumberFormatException e) {
			result.rejectValue("price", "error.price", "Giá không hợp lệ");
		}

		try {
			double price2 = Double.parseDouble(product.getPrice2());
			if (price2 <= 0) {
				result.rejectValue("price2", "error.price2", "Giá khuyến mãi phải lớn hơn 0");
			}
		} catch (NumberFormatException e) {
			result.rejectValue("price2", "error.price2", "Giá khuyến mãi không hợp lệ");
		}
	}

	private boolean isLoggedIn(HttpSession session) {
		return session.getAttribute("loggedInUser") != null;
	}

	@GetMapping("/detail/{id}")
	public String productDetail(@PathVariable("id") String id, Model model, HttpSession session,
			RedirectAttributes redirectAttributes) {

		if (id == null || id.isBlank()) {
			redirectAttributes.addFlashAttribute("error", "ID không hợp lệ");
			return "redirect:/products/list";
		}

		Optional<Product> productOpt = bookRepo.findById(id);
		if (productOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
			return "redirect:/products/list";
		}

		Product product = productOpt.get();

		String categoryName = "Không rõ";
		if (product.getCategoryId() != null && !product.getCategoryId().isBlank()) {
			Category category = categoryRepo.findById(product.getCategoryId()).orElse(null);
			if (category != null) {
				categoryName = category.getName();
			}
		}

		List<String> genreNames = new ArrayList<>();
		if (product.getGenres() != null) {
			for (String genreId : product.getGenres()) {
				if (genreId != null && !genreId.isBlank()) {
					Genre genre = genreRepo.findById(genreId).orElse(null);
					if (genre != null)
						genreNames.add(genre.getName());
				}
			}
		}

		model.addAttribute("product", product);
		model.addAttribute("categoryName", categoryName);
		model.addAttribute("genreNames", genreNames);

		return "product_detail";
	}

	@GetMapping("/product/{id}")
	public String getProductDetail(@PathVariable("id") String id, Model model, HttpSession session) {
		Optional<Product> book = bookRepo.findById(id);
		if (book.isPresent()) {
			Product product = book.get();

			String categoryName = "Không rõ";
			if (product.getCategoryId() != null && !product.getCategoryId().isBlank()) {
				Category category = categoryRepo.findById(product.getCategoryId()).orElse(null);
				if (category != null) {
					categoryName = category.getName();
				}
			}

			// Lấy danh sách sách giảm giá nhiều nhất (ví dụ 20 cuốn)
			List<Product> topDiscountedBooks = bookDao.getTopDiscountedBooks(20);
			List<Product> suggestedBooks = bookDao.getTopDiscountedBooks(10); // lấy 10 quyển gợi ý
			model.addAttribute("suggestedBooks", suggestedBooks);

			// Thêm dữ liệu vào model
			model.addAttribute("book", product);
			model.addAttribute("categoryName", categoryName);
			model.addAttribute("topDiscountedBooks", topDiscountedBooks); // thêm dòng này

			return "home_detail";
		} else {
			return "redirect:/home";
		}
	}
}
