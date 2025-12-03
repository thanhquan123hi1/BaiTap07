package vn.Quan.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.Quan.entity.User;
import vn.Quan.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthController {

    @Autowired
    UserService userService;

    // --- XỬ LÝ ĐĂNG NHẬP ---
    @GetMapping("/login")
    public String loginPage(HttpServletRequest req, HttpSession session, ModelMap model) {
        // Kiểm tra session
        if (session.getAttribute("account") != null) {
            return "redirect:/waiting";
        }

        // Kiểm tra Cookie (Giữ nguyên logic của bạn)
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("username")) {
                    session.setAttribute("username", cookie.getValue());
                    return "redirect:/waiting";
                }
            }
        }
        return "views/login"; // Trả về login.jsp
    }

    @PostMapping("/login")
    public String loginProcess(HttpSession session, ModelMap model,
                               @RequestParam("username") String username,
                               @RequestParam("password") String password) {
        
        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("alert", "Tài khoản hoặc mật khẩu không được rỗng");
            return "views/login";
        }

        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("account", user);
            return "redirect:/waiting";
        } else {
            model.addAttribute("alert", "Tài khoản hoặc mật khẩu không đúng");
            return "views/login";
        }
    }

    // XỬ LÝ ĐIỀU HƯỚNG (WAITING)
    @GetMapping("/waiting")
    public String waiting(HttpSession session) {
        User user = (User) session.getAttribute("account");
        if (user != null) {
            session.setAttribute("username", user.getUsername());
            if (user.isAdmin()) {
                return "redirect:/admin/home";
            } else {
                return "redirect:/home"; 
            }
        }
        return "redirect:/login";
    }

    // --- XỬ LÝ ĐĂNG KÝ ---
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/home";
        }
        return "views/register"; // Trả về register.jsp
    }

    @PostMapping("/register")
    public String registerProcess(ModelMap model,
                                  @RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam("email") String email,
                                  @RequestParam("fullname") String fullname,
                                  @RequestParam("phone") String phone) {
        
        if (userService.existsByEmail(email)) {
            model.addAttribute("alert", "Email đã tồn tại!");
            return "views/register";
        }
        if (userService.existsByUsername(username)) {
            model.addAttribute("alert", "Tài khoản đã tồn tại!");
            return "views/register";
        }

        boolean isSuccess = userService.Register(email, password, username, fullname, phone);
        if (isSuccess) {
            return "redirect:/login";
        } else {
            model.addAttribute("alert", "System error!");
            return "views/register";
        }
    }

    // --- XỬ LÝ ĐĂNG XUẤT ---
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse resp) {
        if (session != null) {
            session.invalidate();
        }
        // Xóa Cookie
        Cookie cookie = new Cookie("username", "");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
        return "redirect:/login";
    }
}