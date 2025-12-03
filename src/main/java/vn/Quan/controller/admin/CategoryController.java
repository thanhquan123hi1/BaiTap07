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

import jakarta.servlet.ServletContext;
import jakarta.validation.Valid;
import vn.Quan.entity.Category;
import vn.Quan.model.CategoryModel;
import vn.Quan.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

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

        Pageable pageable = PageRequest.of(page, size, Sort.by("categoryId").ascending());
        Page<Category> pageResult;

        if (StringUtils.hasText(keyword)) {
            pageResult = categoryService.findByCategoryNameContaining(keyword, pageable);
        } else {
            pageResult = categoryService.findAll(pageable);
        }

        model.addAttribute("categories", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "views/admin/categories/list";
    }

    @GetMapping("add")
    public String add(ModelMap model) {
        CategoryModel cateModel = new CategoryModel();
        cateModel.setIsEdit(false);
        model.addAttribute("category", cateModel);
        return "views/admin/categories/addOrEdit";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(ModelMap model, @PathVariable("id") int id) {
        Optional<Category> optCate = categoryService.findById(id);

        if (optCate.isPresent()) {
            Category category = optCate.get();

            CategoryModel cateModel = new CategoryModel();
            BeanUtils.copyProperties(category, cateModel);
            cateModel.setIsEdit(true);

            model.addAttribute("category", cateModel);
            return new ModelAndView("views/admin/categories/addOrEdit", model);
        }

        model.addAttribute("message", "Category not found!");
        return new ModelAndView("redirect:/admin/categories", model);
    }

    // Xử lý Lưu (Thêm mới hoặc Cập nhật)
    @PostMapping("/saveOrUpdate")
    public ModelAndView saveOrUpdate(ModelMap model,
            @Valid @ModelAttribute("category") CategoryModel cateModel,
            BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("views/admin/categories/addOrEdit");
        }

        Category entity = new Category();
        BeanUtils.copyProperties(cateModel, entity);

        // Xử lý Upload File
        if (cateModel.getImageFile() != null && !cateModel.getImageFile().isEmpty()) {
            try {
                String fileName = cateModel.getImageFile().getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/upload/category/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (var inputStream = cateModel.getImageFile().getInputStream()) {
                    Files.copy(inputStream, uploadPath.resolve(fileName),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                entity.setImages(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("message", "Error uploading file: " + e.getMessage());
            }
        } else {
            // Nếu edit mà không chọn ảnh mới -> giữ ảnh cũ
            if (cateModel.getIsEdit() != null && cateModel.getIsEdit()) {
                Category category = categoryService.findById(cateModel.getCategoryId()).orElse(null);

                if (category != null) {
                    entity.setImages(category.getImages());
                }
            }
        }

        categoryService.save(entity);
        boolean isEdit = cateModel.getIsEdit() != null && cateModel.getIsEdit();
        String message = isEdit ? "Cập nhật thành công!" : "Thêm mới thành công!";
        model.addAttribute("message", message);

        return new ModelAndView("redirect:/admin/categories", model);
    }

    @GetMapping("/delete/{id}")
    public ModelAndView delete(ModelMap model, @PathVariable("id") int id) {
        try {
            categoryService.deleteById(id);
            model.addAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi khi xóa: " + e.getMessage());
        }
        return new ModelAndView("redirect:/admin/categories", model);
    }
}
