package vn.Quan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoRequestDTO {
    
    @NotEmpty(message = "Video ID không được để trống")
    private String videoId;

    @NotEmpty(message = "Tiêu đề video không được để trống")
    private String title;

    private String poster;
    private String description;
    
    @Min(value = 0, message = "Lượt xem phải lớn hơn hoặc bằng 0")
    private int views = 0;

    private boolean active = true;
    private Integer categoryId;
}

