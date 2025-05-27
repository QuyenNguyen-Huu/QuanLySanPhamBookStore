package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import model.Category;
import model.Genre;

@Repository
public interface GenreRepository extends MongoRepository<Genre, String> {
	// Tìm danh mục theo tên
	Genre findByName(String name);

    // Hoặc nếu có nhiều danh mục trùng tên, lấy danh sách
    List<Genre> findAllByName(String name);
    }
