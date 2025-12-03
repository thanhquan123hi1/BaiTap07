package vn.Quan.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.Quan.entity.Video;
import vn.Quan.repository.VideoRepository;
import vn.Quan.service.VideoService;

@Service
public class VideoServiceImpl implements VideoService {

	@Autowired
	VideoRepository videoRepository;

	@Override
	public List<Video> findByTitleContaining(String title) {
		return videoRepository.findByTitleContaining(title);
	}

	@Override
	public <S extends Video> S save(S entity) {
		return videoRepository.save(entity);
	}

	@Override
	public List<Video> findAll() {
		return videoRepository.findAll();
	}

	@Override
	public Page<Video> findAll(Pageable pageable) {
		return videoRepository.findAll(pageable);
	}

	@Override
	public Optional<Video> findById(String id) {
		return videoRepository.findById(id);
	}

	@Override
	public void deleteById(String id) {
		videoRepository.deleteById(id);
	}

	@Override
	public Page<Video> findByTitleContaining(String title, Pageable pageable) {
		return videoRepository.findByTitleContaining(title, pageable);
	}

	@Override
	public long count() {
		return videoRepository.count();
	}
}
