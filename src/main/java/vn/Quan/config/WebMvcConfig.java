package vn.Quan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình đường dẫn: Khi truy cập /upload/** sẽ tìm file trong thư mục 'upload' ở gốc dự án
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:upload/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**", "/user/**")  // Các đường dẫn cần xác thực
                .excludePathPatterns("/login", "/register", "/logout", "/", "/home", "/videos", "/video/**", "/categories", "/category/**", "/css/**", "/js/**", "/images/**", "/upload/**");
    }
}