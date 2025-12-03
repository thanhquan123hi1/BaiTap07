package vn.Quan.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "videos")
public class Video implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
    @Column(name = "videoid", length = 50)
    private String videoId; // VideoID dạng chuỗi

    @Column(name = "title", columnDefinition = "nvarchar(100)")
    private String title;

    @Column(name = "poster", columnDefinition = "nvarchar(200)")
    private String poster;

    @Column(name = "views")
    private int views;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active")
    private boolean active;

    // Quan hệ N-1: Nhiều Video thuộc về 1 Category
    @ManyToOne
    @JoinColumn(name = "categoryid")
    private Category category;

}
