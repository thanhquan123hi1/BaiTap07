package vn.Quan.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryModel {

    private int categoryId;

    @NotEmpty(message = "Tên danh mục không được để trống")
    private String categoryName;

    private String categoryCode;

    private String images;

    // Hứng file upload
    private MultipartFile imageFile;

    private int status = 1; // Mặc định Active

    // Quan hệ N-1: Chỉ cần lưu username của người tạo/quản lý danh mục này
    private String username;

    private Boolean isEdit = false;
}