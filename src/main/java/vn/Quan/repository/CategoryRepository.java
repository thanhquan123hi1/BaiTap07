package vn.Quan.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.Quan.entity.Category;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
	// Tìm Kiếm theo nội dung tên
	List<Category> findByCategoryNameContaining(String name);

	// Tìm kiếm và Phân trang
	Page<Category> findByCategoryNameContaining(String name, Pageable pageable);
}
