package repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
}
