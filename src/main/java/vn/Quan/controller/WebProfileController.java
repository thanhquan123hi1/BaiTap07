package vn.Quan.controller;


import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import vn.Quan.entity.User;
import vn.Quan.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequestMapping({"/admin/profile", "/user/profile"}) // Map cả 2 đường dẫn như cũ
public class WebProfileController {

    @Autowired
    UserService userService;

    @Autowired
    ServletContext application;

    @GetMapping("")
    public String profilePage(HttpSession session, ModelMap model) {
        User sessionUser = (User) session.getAttribute("account");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        Optional<User> optUser = userService.findById(sessionUser.getUsername());
        
        if (optUser.isPresent()) {
            model.addAttribute("user", optUser.get());
            return "views/web/profile";
        } else {
            // Trường hợp hy hữu: Session còn nhưng User trong DB bị xóa
            session.invalidate();
            return "redirect:/login";
        }
    }

    @PostMapping("")
    public String updateProfile(HttpSession session, ModelMap model,
                                @RequestParam("fullname") String fullname,
                                @RequestParam("phone") String phone,
                                @RequestParam("email") String email,
                                @RequestParam("images") MultipartFile imageFile) {
        
        User sessionUser = (User) session.getAttribute("account");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        try {
            Optional<User> optUser = userService.findById(sessionUser.getUsername());
            
            if (optUser.isPresent()) {
                User userToUpdate = optUser.get(); // Lấy user thật ra

                userToUpdate.setFullname(fullname);
                userToUpdate.setPhone(phone);
                userToUpdate.setEmail(email);

                if (!imageFile.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                    String uploadDir = System.getProperty("user.dir") + "/upload/user/";;
                    Path uploadPath = Paths.get(uploadDir);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    try (var inputStream = imageFile.getInputStream()) {
                        Files.copy(inputStream, uploadPath.resolve(fileName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    userToUpdate.setImages(fileName);
                }

                userService.save(userToUpdate);

                session.setAttribute("account", userToUpdate);
                model.addAttribute("message", "Cập nhật thông tin thành công!");
                model.addAttribute("user", userToUpdate);
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "views/web/profile";
    }
}