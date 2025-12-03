package vn.Quan.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.Quan.entity.User;
import vn.Quan.repository.UserRepository;
import vn.Quan.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Override
	public Optional<User> findByUsernameAndPassword(String username, String password) {
		return userRepository.findByUsernameAndPassword(username, password);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public <S extends User> S save(S entity) {
		return userRepository.save(entity);
	}

	@Override
	public Optional<User> findById(String id) {
		return userRepository.findById(id);
	}

	@Override
	public boolean existsById(String id) {
		return userRepository.existsById(id);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public User login(String username, String password) {
		return userRepository.findByUsernameAndPassword(username, password).orElse(null);
	}

	@Override
	public boolean Register(String email, String password, String username, String fullname, String phone) {
		// Kiểm tra xem username hoặc email đã tồn tại chưa
		if (userRepository.existsById(username)) {
			return false;
		}
		if (userRepository.existsByEmail(email)) {
			return false;
		}

		// Tạo user mới
		User newUser = new User();
		newUser.setUsername(username);
		newUser.setPassword(password);
		newUser.setEmail(email);
		newUser.setFullname(fullname);
		newUser.setPhone(phone);
		newUser.setAdmin(false);
		newUser.setActive(true);

		userRepository.save(newUser);
		return true;
	}

	@Override
	@Transactional
	public void updatePassword(String id, String newpass) {
		User user = userRepository.getById(id);
		if (user != null) {
			user.setPassword(newpass);
			userRepository.save(user);
		}
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	public Page<User> findAll(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Override
	public void deleteById(String id) {
		userRepository.deleteById(id);
	}

	@Override
	public List<User> findByUsernameContainingOrFullnameContaining(String username, String fullname) {
		return userRepository.findByUsernameContainingOrFullnameContaining(username, fullname);
	}

	@Override
	public Page<User> findByUsernameContainingOrFullnameContaining(String username, String fullname,
			Pageable pageable) {
		return userRepository.findByUsernameContainingOrFullnameContaining(username, fullname, pageable);
	}

	@Override
	public long count() {
		return userRepository.count();
	}
}
