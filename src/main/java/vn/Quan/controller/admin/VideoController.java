package vn.Quan.controller.admin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.ServletContext;
import jakarta.validation.Valid;
import vn.Quan.entity.Category;
import vn.Quan.entity.Video;
import vn.Quan.model.VideoModel;
import vn.Quan.service.CategoryService;
import vn.Quan.service.VideoService;

@Controller
@RequestMapping("/admin/videos")
public class VideoController {

    @Autowired
    VideoService videoService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ServletContext application;

    // Danh sách với phân trang và tìm kiếm
    @GetMapping("")
    public String list(ModelMap model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("videoId").ascending());
        Page<Video> pageResult;

        if (StringUtils.hasText(keyword)) {
            pageResult = videoService.findByTitleContaining(keyword, pageable);
        } else {
            pageResult = videoService.findAll(pageable);
        }

        model.addAttribute("videos", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "views/admin/videos/list";
    }

    // Trang quản lý video với AJAX
    @GetMapping("/ajax")
    public String listAjax(ModelMap model) {
        // Chỉ cần load categories cho dropdown
        model.addAttribute("categories", categoryService.findAll());
        return "views/admin/videos/list-ajax";
    }

    // Form Thêm mới
    @GetMapping("/add")
    public String add(ModelMap model) {
        VideoModel videoModel = new VideoModel();
        videoModel.setIsEdit(false);
        model.addAttribute("video", videoModel);
        return "views/admin/videos/addOrEdit";
    }

    // Form Sửa
    @GetMapping("/edit/{id}")
    public ModelAndView edit(ModelMap model, @PathVariable("id") String id) {
        Optional<Video> optVideo = videoService.findById(id);

        if (optVideo.isPresent()) {
            Video video = optVideo.get();

            VideoModel videoModel = new VideoModel();
            BeanUtils.copyProperties(video, videoModel);
            videoModel.setIsEdit(true);

            if (video.getCategory() != null) {
                videoModel.setCategoryId(video.getCategory().getCategoryId());
            }

            model.addAttribute("video", videoModel);
            return new ModelAndView("views/admin/videos/addOrEdit", model);
        }

        model.addAttribute("message", "Video not found!");
        return new ModelAndView("redirect:/admin/videos", model);
    }

    // ModelAttribute dùng chung cho cả trang Add và Edit để đổ dữ liệu Category vào
    // dropdown
    @ModelAttribute("categories")
    public List<Category> getCategories() {
        return categoryService.findAll();
    }

    @PostMapping("/saveOrUpdate")
    public ModelAndView saveOrUpdate(ModelMap model,
            @Valid @ModelAttribute("video") VideoModel videoModel,
            BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("views/admin/videos/addOrEdit");
        }

        Video entity = new Video();
        BeanUtils.copyProperties(videoModel, entity);

        Category category = new Category();
        category.setCategoryId(videoModel.getCategoryId());
        entity.setCategory(category);

        if (videoModel.getPosterFile() != null && !videoModel.getPosterFile().isEmpty()) {
            try {
                String fileName = videoModel.getPosterFile().getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/upload/video/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath))
                    Files.createDirectories(uploadPath);

                try (var inputStream = videoModel.getPosterFile().getInputStream()) {
                    Files.copy(inputStream, uploadPath.resolve(fileName),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                entity.setPoster(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (videoModel.getIsEdit()) {
                Video oldVideo = videoService.findById(videoModel.getVideoId()).orElse(null);
                if (oldVideo != null) {
                    entity.setPoster(oldVideo.getPoster());
                }
            }
        }

        videoService.save(entity);

        String message = videoModel.getIsEdit() ? "Cập nhật thành công!" : "Thêm mới thành công!";
        model.addAttribute("message", message);

        return new ModelAndView("redirect:/admin/videos", model);
    }

    @GetMapping("/delete/{id}")
    public ModelAndView delete(ModelMap model, @PathVariable("id") String id) {
        try {
            videoService.deleteById(id);
            model.addAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi khi xóa: " + e.getMessage());
        }
        return new ModelAndView("redirect:/admin/videos", model);
    }
}
