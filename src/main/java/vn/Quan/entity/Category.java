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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "categories") 
public class Category implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoryid")
    private int categoryId;

    @Column(name = "categoryname", columnDefinition = "nvarchar(255)")
    private String categoryName;

    @Column(name = "categorycode", length = 50)
    private String categoryCode;

    @Column(name = "images", columnDefinition = "nvarchar(500)")
    private String images;

    @Column(name = "status")
    private int status;

    // Quan hệ N-1: Nhiều Category thuộc về 1 User
    @ManyToOne
    @JoinColumn(name = "username") // Tên cột khóa ngoại trong DB
    private User user;

    // Quan hệ 1-N: Category chứa nhiều Video
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Video> videos;
	
}
