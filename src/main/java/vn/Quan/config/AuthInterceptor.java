package vn.Quan.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.Quan.entity.User;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("account");
        String requestURI = request.getRequestURI();

        // Nếu chưa đăng nhập, chuyển hướng đến trang login
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Kiểm tra quyền truy cập trang admin
        if (requestURI.startsWith("/admin")) {
            if (!user.isAdmin()) {
                // Nếu không phải admin, chuyển hướng về trang home của user
                response.sendRedirect("/home");
                return false;
            }
        }

        return true;
    }
}
