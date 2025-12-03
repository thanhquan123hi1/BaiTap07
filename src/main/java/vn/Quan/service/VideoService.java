package vn.Quan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.Quan.entity.Video;

public interface VideoService {

	void deleteById(String id);

	Optional<Video> findById(String id);

	Page<Video> findAll(Pageable pageable);

	List<Video> findAll();

	<S extends Video> S save(S entity);

	List<Video> findByTitleContaining(String title);

	Page<Video> findByTitleContaining(String title, Pageable pageable);

	long count();

}
