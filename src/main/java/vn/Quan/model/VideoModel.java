package vn.Quan.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoModel {

    @NotEmpty(message = "Video ID không được để trống")
    private String videoId;

    @NotEmpty(message = "Tiêu đề video không được để trống")
    private String title;

    private String poster;
    
    // Hứng file poster upload
    private MultipartFile posterFile;

    @Min(value = 0, message = "Lượt xem phải lớn hơn hoặc bằng 0")
    private int views = 0;

    private String description;

    private boolean active = true;

    // Quan hệ N-1: Lưu ID của danh mục mà video thuộc về
    private int categoryId;

    private Boolean isEdit = false;
}