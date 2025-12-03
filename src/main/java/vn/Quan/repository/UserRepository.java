package vn.Quan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.Quan.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByUsernameAndPassword(String username, String password);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	// Tìm kiếm theo username hoặc fullname
	List<User> findByUsernameContainingOrFullnameContaining(String username, String fullname);

	// Tìm kiếm và phân trang
	Page<User> findByUsernameContainingOrFullnameContaining(String username, String fullname, Pageable pageable);

}
