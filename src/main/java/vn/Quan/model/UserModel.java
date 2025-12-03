package vn.Quan.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải từ 6 ký tự trở lên")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullname;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 số")
    private String phone;

    private String images;
    
    // Trường này dùng để hứng file từ form (type="file")
    private MultipartFile imageFile;

    private boolean admin = false;

    private boolean active = true;

    // Cờ để xác định đang ở chế độ Thêm mới hay Chỉnh sửa
    private Boolean isEdit = false;
}