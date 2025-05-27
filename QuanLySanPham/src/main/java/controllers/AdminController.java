package controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/products")
    public String manageProducts(Model model , HttpSession session) {
        // lấy dữ liệu sản phẩm ở đây
        return "redirect:/products/list"; 
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        // lấy dữ liệu người dùng ở đây
        return "admin/users"; 
    }
}
