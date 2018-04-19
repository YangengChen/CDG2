package cdg.repository;

import org.springframework.data.repository.CrudRepository;

import cdg.dao.User;;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, String> {

}