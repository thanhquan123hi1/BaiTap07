package vn.Quan.controller.admin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import jakarta.validation.Valid;
import vn.Quan.entity.User;
import vn.Quan.model.UserModel;
import vn.Quan.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    UserService userService;

    // Danh sách với phân trang và tìm kiếm
    @GetMapping("")
    public String list(ModelMap model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<User> pageResult;

        if (StringUtils.hasText(keyword)) {
            pageResult = userService.findByUsernameContainingOrFullnameContaining(keyword, keyword, pageable);
        } else {
            pageResult = userService.findAll(pageable);
        }

        model.addAttribute("users", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "views/admin/users/list";
    }

    // Form Thêm mới
    @GetMapping("/add")
    public String add(ModelMap model) {
        UserModel userModel = new UserModel();
        userModel.setIsEdit(false);
        model.addAttribute("user", userModel);
        return "views/admin/users/addOrEdit";
    }

    // Form Sửa
    @GetMapping("/edit/{id}")
    public ModelAndView edit(ModelMap model, @PathVariable("id") String id) {
        Optional<User> optUser = userService.findById(id);

        if (optUser.isPresent()) {
            User user = optUser.get();

            UserModel userModel = new UserModel();
            BeanUtils.copyProperties(user, userModel);
            userModel.setIsEdit(true);
            userModel.setPassword(""); // Không hiển thị password

            model.addAttribute("user", userModel);
            return new ModelAndView("views/admin/users/addOrEdit", model);
        }

        model.addAttribute("message", "User not found!");
        return new ModelAndView("redirect:/admin/users", model);
    }

    // Xử lý Lưu (Thêm mới hoặc Cập nhật)
    @PostMapping("/saveOrUpdate")
    public ModelAndView saveOrUpdate(ModelMap model,
            @Valid @ModelAttribute("user") UserModel userModel,
            BindingResult result) {

        // Nếu là thêm mới, kiểm tra username đã tồn tại chưa
        if (!userModel.getIsEdit() && userService.existsByUsername(userModel.getUsername())) {
            result.rejectValue("username", "error.user", "Username đã tồn tại!");
        }

        // Nếu thêm mới thì bắt buộc có password
        if (!userModel.getIsEdit() && !StringUtils.hasText(userModel.getPassword())) {
            result.rejectValue("password", "error.user", "Password không được để trống!");
        }

        if (result.hasErrors()) {
            return new ModelAndView("views/admin/users/addOrEdit");
        }

        User entity;

        if (userModel.getIsEdit()) {
            // Nếu là edit, lấy entity cũ để giữ password nếu không đổi
            entity = userService.findById(userModel.getUsername()).orElse(new User());
            String oldPassword = entity.getPassword();
            String oldImage = entity.getImages();

            BeanUtils.copyProperties(userModel, entity);

            // Giữ password cũ nếu không nhập mới
            if (!StringUtils.hasText(userModel.getPassword())) {
                entity.setPassword(oldPassword);
            }

            // Xử lý Upload File
            if (userModel.getImageFile() != null && !userModel.getImageFile().isEmpty()) {
                try {
                    String fileName = userModel.getImageFile().getOriginalFilename();
                    String uploadDir = System.getProperty("user.dir") + "/upload/user/";
                    Path uploadPath = Paths.get(uploadDir);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    try (var inputStream = userModel.getImageFile().getInputStream()) {
                        Files.copy(inputStream, uploadPath.resolve(fileName),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    entity.setImages(fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    model.addAttribute("message", "Error uploading file: " + e.getMessage());
                }
            } else {
                // Giữ ảnh cũ
                entity.setImages(oldImage);
            }
        } else {
            // Thêm mới
            entity = new User();
            BeanUtils.copyProperties(userModel, entity);

            // Xử lý Upload File
            if (userModel.getImageFile() != null && !userModel.getImageFile().isEmpty()) {
                try {
                    String fileName = userModel.getImageFile().getOriginalFilename();
                    String uploadDir = System.getProperty("user.dir") + "/upload/user/";
                    Path uploadPath = Paths.get(uploadDir);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    try (var inputStream = userModel.getImageFile().getInputStream()) {
                        Files.copy(inputStream, uploadPath.resolve(fileName),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    entity.setImages(fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        userService.save(entity);

        String message = userModel.getIsEdit() ? "Cập nhật thành công!" : "Thêm mới thành công!";
        model.addAttribute("message", message);

        return new ModelAndView("redirect:/admin/users", model);
    }

    @GetMapping("/delete/{id}")
    public ModelAndView delete(ModelMap model, @PathVariable("id") String id) {
        try {
            userService.deleteById(id);
            model.addAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi khi xóa: " + e.getMessage());
        }
        return new ModelAndView("redirect:/admin/users", model);
    }
}
