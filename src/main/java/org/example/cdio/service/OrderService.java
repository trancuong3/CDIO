package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.*;
import org.example.cdio.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final InventoryService inventoryService;
    // thêm email service
    private final EmailService emailService;


    public Order create(Store store, Map<Product, Integer> items) {



        Order order = new Order();
        order.setStore(store);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (var e : items.entrySet()) {

            Product p = e.getKey();
            Integer qty = e.getValue();

            BigDecimal price = p.getWholesalePrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(qty);
            oi.setUnitPrice(price);
            oi.setLineTotal(lineTotal);

            order.getItems().add(oi);

            total = total.add(lineTotal);
        }

        //order.setTotalAmount(total);
        // gửi mail
//        Order saved = orderRepo.save(order);
//
//        emailService.sendEmail(
//                "testgpttrial22@gmail.com",
//                "Đơn hàng mới",
//                "Có đơn hàng mới với tổng tiền: " + saved.getTotalAmount()
//        );
//
//        return saved;

        Order savedOrder = orderRepo.save(order);

        // Nội dung email
        String subject = "Đơn hàng mới từ hệ thống Khô Gà";

        String content = "Xin chào,\n\n"
                + "Một đơn hàng mới vừa được tạo.\n"
                + "Mã đơn hàng: #" + savedOrder.getId() + "\n"
                + "Tổng tiền: " + savedOrder.getTotalAmount() + " VNĐ\n"
                + "Trạng thái: " + savedOrder.getStatus() + "\n\n"
                + "Vui lòng đăng nhập hệ thống để kiểm tra chi tiết.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống quản lý Khô Gà Độ Chó.";

        // gửi email khi tạo đơn hàng

        emailService.sendEmail(
                "testgpttrial22@gmail.com",
                subject,
                content
        );

        return savedOrder;


    }

    // ADMIN DUYỆT
    public void approve(Long id) {

        Order o = orderRepo.findById(id).orElseThrow();

        for (OrderItem i : o.getItems()) {
            inventoryService.stockOut(i.getProduct().getId(), i.getQuantity());
        }

        o.setStatus(OrderStatus.APPROVED);

        String subject = "Đơn hàng đã được duyệt";

        String content = "Xin chào,\n\n"
                + "Đơn hàng #" + o.getId() + " của bạn đã được duyệt thành công.\n"
                + "Tổng tiền: " + o.getTotalAmount() + " VNĐ\n"
                + "Trạng thái: " + o.getStatus() + "\n\n"
                + "Vui lòng chuẩn bị nhận hàng.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống Khô Gà ĐỘ Chó.";

        emailService.sendEmail(
                "testgpttrial22@gmail.com",
                subject,
                content
        );
//        emailService.sendEmail(
//                "testgpttrial22@gmail.com",
//                "Đơn hàng đã được duyệt",
//                "Đơn hàng #" + o.getId() + " đã được duyệt."
//        );
    }

    public void createOrder() {

        System.out.println("Order created!");

        String subject = "Đơn hàng mới từ hệ thống Khô Gà";

        String content = "Xin chào,\n\n"
                + "Một đơn hàng mới vừa được tạo trong hệ thống.\n"
                + "Vui lòng đăng nhập vào hệ thống để kiểm tra chi tiết.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống quản lý Khô Gà ĐỘ CHó.";

        emailService.sendEmail(
                "testgpttrial22@gmail.com",
                subject,
                content
        );
    }
}
