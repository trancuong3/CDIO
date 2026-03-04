package org.example.cdio.controller;

import org.example.cdio.entity.Product;
import org.example.cdio.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/store")
public class StoreController {

    // Tiêm (Inject) cái thủ kho ProductRepository vào đây để dùng
    @Autowired
    private ProductRepository productRepository;

    // Chú ý: Ta thêm đối tượng Model vào tham số hàm.
    // Model chính là "chiếc giỏ" để chở dữ liệu từ Java sang HTML
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        // 1. Nhờ thủ kho lấy toàn bộ sản phẩm trong bảng products lên
        List<Product> danhSachKhoGa = productRepository.findAll();

        // 2. Bỏ danh sách này vào "chiếc giỏ" Model, đặt tên nhãn dán là "products"
        model.addAttribute("products", danhSachKhoGa);

        // 3. Trả về tên file HTML (Thymeleaf sẽ mở file này lên và tìm cái nhãn "products" kia)
        return "store/store-dashboard";
    }
}