package vn.Quan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.Quan.entity.Category;
import vn.Quan.entity.Video;
import vn.Quan.service.CategoryService;
import vn.Quan.service.VideoService;

@Controller
public class HomeWebController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    VideoService videoService;

    // Trang chủ
    @GetMapping({ "/", "/home" })
    public String home(ModelMap model) {
        List<Category> categories = categoryService.findAll();
        List<Video> latestVideos = videoService.findAll();

        // Lấy 8 video mới nhất
        int limit = Math.min(8, latestVideos.size());
        List<Video> videos = latestVideos.subList(0, limit);

        model.addAttribute("categories", categories);
        model.addAttribute("videos", videos);

        return "views/web/home";
    }

    // Danh sách video với phân trang
    @GetMapping("/videos")
    public String videos(ModelMap model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("videoId").descending());
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

        return "views/web/videos";
    }

    // Chi tiết video
    @GetMapping("/video/{id}")
    public String videoDetail(ModelMap model, @PathVariable("id") String id) {
        Video video = videoService.findById(id).orElse(null);

        if (video == null) {
            return "redirect:/videos";
        }

        // Tăng lượt xem
        video.setViews(video.getViews() + 1);
        videoService.save(video);

        // Video liên quan (cùng danh mục)
        List<Video> relatedVideos = videoService.findAll().stream()
                .filter(v -> v.getCategory() != null && video.getCategory() != null
                        && v.getCategory().getCategoryId() == video.getCategory().getCategoryId()
                        && !v.getVideoId().equals(video.getVideoId()))
                .limit(4)
                .toList();

        model.addAttribute("video", video);
        model.addAttribute("relatedVideos", relatedVideos);

        return "views/web/videoDetail";
    }

    // Danh sách danh mục
    @GetMapping("/categories")
    public String categories(ModelMap model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "views/web/categories";
    }

    // Video theo danh mục
    @GetMapping("/category/{id}")
    public String videosByCategory(ModelMap model,
            @PathVariable("id") int id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {

        Category category = categoryService.findById(id).orElse(null);

        if (category == null) {
            return "redirect:/categories";
        }

        // Lấy video của category này
        List<Video> allVideos = videoService.findAll().stream()
                .filter(v -> v.getCategory() != null && v.getCategory().getCategoryId() == id)
                .toList();

        int start = page * size;
        int end = Math.min(start + size, allVideos.size());
        List<Video> videos = allVideos.subList(start, end);

        int totalPages = (int) Math.ceil((double) allVideos.size() / size);

        model.addAttribute("category", category);
        model.addAttribute("videos", videos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", allVideos.size());
        model.addAttribute("size", size);

        return "views/web/categoryVideos";
    }
}
