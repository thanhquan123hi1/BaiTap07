package vn.Quan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.Quan.entity.User;

public interface UserService {

	boolean existsById(String id);

	Optional<User> findById(String id);

	<S extends User> S save(S entity);

	Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<User> findByUsernameAndPassword(String username, String password);

	User login(String username, String password);

	Optional<User> findByUsername(String username);

	boolean Register(String email, String password, String username, String fullname, String phone);

	void updatePassword(String id, String newpass);

	// Thêm các method mới cho CRUD và phân trang
	List<User> findAll();

	Page<User> findAll(Pageable pageable);

	void deleteById(String id);

	List<User> findByUsernameContainingOrFullnameContaining(String username, String fullname);

	Page<User> findByUsernameContainingOrFullnameContaining(String username, String fullname, Pageable pageable);

	long count();
}
