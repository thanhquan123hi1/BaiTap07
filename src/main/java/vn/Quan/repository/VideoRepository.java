package vn.Quan.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.Quan.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
	// Tìm kiếm theo tiêu đề
	List<Video> findByTitleContaining(String title);

	// Tìm kiếm và phân trang
	Page<Video> findByTitleContaining(String title, Pageable pageable);
}
