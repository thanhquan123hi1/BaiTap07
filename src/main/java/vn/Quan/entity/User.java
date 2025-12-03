package vn.Quan.entity;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
@Table(name = "users")
public class User implements Serializable{

	private static final long serialVersionUID = 1L;


	@Id
    @Column(name = "username", length = 50, nullable = false)
    private String username; // Dùng username làm khóa chính

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "fullname", columnDefinition = "nvarchar(255)")
    private String fullname;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "images", columnDefinition = "nvarchar(500)")
    private String images;

    @Column(name = "admin")
    private boolean admin;

    @Column(name = "active")
    private boolean active;

    // Quan hệ 1-N: User quản lý nhiều Category
    // mappedBy trỏ tới tên biến 'user' trong class Category
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> categories;
	
	

}
