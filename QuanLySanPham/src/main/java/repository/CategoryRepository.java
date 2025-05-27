package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import model.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
	
	// Tìm danh mục theo tên
    Category findByName(String name);

    // Hoặc nếu có nhiều danh mục trùng tên, lấy danh sách
    List<Category> findAllByName(String name);
}