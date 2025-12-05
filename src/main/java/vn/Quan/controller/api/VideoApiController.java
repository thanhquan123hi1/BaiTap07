package vn.Quan.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.Quan.dto.ApiResponse;
import vn.Quan.dto.PageResponse;
import vn.Quan.dto.VideoDTO;
import vn.Quan.entity.Category;
import vn.Quan.entity.Video;
import vn.Quan.model.VideoModel;
import vn.Quan.service.CategoryService;
import vn.Quan.service.VideoService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos")
@Tag(name = "Video API", description = "API quản lý Video với CRUD, phân trang và tìm kiếm")
@CrossOrigin(origins = "*")
public class VideoApiController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Lấy danh sách video có phân trang và tìm kiếm")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VideoDTO>>> getAllVideos(
            @Parameter(description = "Từ khóa tìm kiếm theo tiêu đề") @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo trường (ví dụ: title, views)") @RequestParam(defaultValue = "videoId") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Video> videoPage;
            
            if (StringUtils.hasText(keyword)) {
                videoPage = videoService.findByTitleContaining(keyword, pageable);
            } else {
                videoPage = videoService.findAll(pageable);
            }
            
            List<VideoDTO> videoDTOs = videoPage.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            PageResponse<VideoDTO> pageResponse = PageResponse.of(
                    videoDTOs,
                    videoPage.getNumber(),
                    videoPage.getTotalPages(),
                    videoPage.getTotalElements(),
                    videoPage.getSize()
            );
            
            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách video: " + e.getMessage()));
        }
    }

    @Operation(summary = "Lấy thông tin video theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDTO>> getVideoById(
            @Parameter(description = "ID của video") @PathVariable String id) {
        try {
            Optional<Video> videoOpt = videoService.findById(id);
            if (videoOpt.isPresent()) {
                VideoDTO dto = convertToDTO(videoOpt.get());
                return ResponseEntity.ok(ApiResponse.success(dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy video với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy video: " + e.getMessage()));
        }
    }

    @Operation(summary = "Tạo video mới")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<VideoDTO>> createVideo(
            @Valid @ModelAttribute VideoModel videoModel) {
        try {
            // Validate
            if (videoService.findById(videoModel.getVideoId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Video ID đã tồn tại"));
            }

            Video entity = new Video();
            BeanUtils.copyProperties(videoModel, entity);

            // Set category
            if (videoModel.getCategoryId() > 0) {
                Optional<Category> categoryOpt = categoryService.findById(videoModel.getCategoryId());
                if (categoryOpt.isPresent()) {
                    entity.setCategory(categoryOpt.get());
                }
            }

            // Handle file upload
            if (videoModel.getPosterFile() != null && !videoModel.getPosterFile().isEmpty()) {
                String fileName = videoModel.getPosterFile().getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/upload/video/";
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                try (var inputStream = videoModel.getPosterFile().getInputStream()) {
                    Files.copy(inputStream, uploadPath.resolve(fileName), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                entity.setPoster(fileName);
            }

            Video savedVideo = videoService.save(entity);
            VideoDTO dto = convertToDTO(savedVideo);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo video thành công", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo video: " + e.getMessage()));
        }
    }

    @Operation(summary = "Cập nhật video")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<VideoDTO>> updateVideo(
            @Parameter(description = "ID của video") @PathVariable String id,
            @Valid @ModelAttribute VideoModel videoModel) {
        try {
            Optional<Video> videoOpt = videoService.findById(id);
            if (!videoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy video với ID: " + id));
            }

            Video existingVideo = videoOpt.get();
            BeanUtils.copyProperties(videoModel, existingVideo, "videoId", "poster");

            // Set category
            if (videoModel.getCategoryId() > 0) {
                Optional<Category> categoryOpt = categoryService.findById(videoModel.getCategoryId());
                if (categoryOpt.isPresent()) {
                    existingVideo.setCategory(categoryOpt.get());
                }
            }

            // Handle file upload
            if (videoModel.getPosterFile() != null && !videoModel.getPosterFile().isEmpty()) {
                String fileName = videoModel.getPosterFile().getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/upload/video/";
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                try (var inputStream = videoModel.getPosterFile().getInputStream()) {
                    Files.copy(inputStream, uploadPath.resolve(fileName), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                existingVideo.setPoster(fileName);
            }

            Video updatedVideo = videoService.save(existingVideo);
            VideoDTO dto = convertToDTO(updatedVideo);
            
            return ResponseEntity.ok(ApiResponse.success("Cập nhật video thành công", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật video: " + e.getMessage()));
        }
    }

    @Operation(summary = "Xóa video")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(
            @Parameter(description = "ID của video") @PathVariable String id) {
        try {
            Optional<Video> videoOpt = videoService.findById(id);
            if (!videoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy video với ID: " + id));
            }

            videoService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa video thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa video: " + e.getMessage()));
        }
    }

    private VideoDTO convertToDTO(Video video) {
        VideoDTO dto = new VideoDTO();
        BeanUtils.copyProperties(video, dto);
        if (video.getCategory() != null) {
            dto.setCategoryId(video.getCategory().getCategoryId());
            dto.setCategoryName(video.getCategory().getCategoryName());
        }
        return dto;
    }
}

