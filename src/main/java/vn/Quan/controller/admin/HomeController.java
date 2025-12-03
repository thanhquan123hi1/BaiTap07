package vn.Quan.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.Quan.service.CategoryService;
import vn.Quan.service.UserService;
import vn.Quan.service.VideoService;

@Controller
@RequestMapping("/admin")
public class HomeController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    VideoService videoService;

    @Autowired
    UserService userService;

    @GetMapping("/home")
    public String index(ModelMap model) {
        long countCategories = categoryService.findAll().size();
        long countVideos = videoService.count();
        long countUsers = userService.count();

        // Tính tổng lượt xem từ tất cả video
        long totalViews = videoService.findAll().stream()
                .mapToLong(v -> v.getViews())
                .sum();

        model.addAttribute("countCategories", countCategories);
        model.addAttribute("countVideos", countVideos);
        model.addAttribute("countUsers", countUsers);
        model.addAttribute("totalViews", totalViews);

        return "views/admin/home";
    }
}
