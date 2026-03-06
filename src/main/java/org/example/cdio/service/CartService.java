package org.example.cdio.service;

import org.example.cdio.dto.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@SessionScope //Lưu giỏ hàng lên RAM theo từng session
public class CartService {
    //Dùng Map với Key là ID sản phầm và Value là chi tiết sản phẩm
    private Map<Long, CartItem> cart = new HashMap<>();

    public void addProduct(Long productId, String name, double price) {
        if (cart.containsKey(productId)) {
            // Nếu sản phầm có trong giỏ thì tăng 1
            CartItem item = cart.get(productId);
            item.setQuantity(item.getQuantity() + 1);
        } else {
            // Nếu chưa có sản phầm thì thêm vào Map với số lượng là 1
            cart.put(productId, new CartItem(productId, name, price, 1));
        }
    }

    // 2. Lấy toàn bộ hàng trong giỏ ra để xem
    public Collection<CartItem> getCartItems() {
        return cart.values();
    }

    // 3. Đếm tổng số lượng bịch khô gà (để hiện lên góc phải)
    public int getTotalCount() {
        return cart.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    // 4. Tính tổng số tiền (Sẽ dùng ở Giai đoạn 2)
    public double getTotalPrice() {
        return cart.values().stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
    }

    // 5. Cập nhật số lượng
    public void updateQuantity(Long productId, int quantity) {
        if (cart.containsKey(productId)) {
            if (quantity <= 0) {
                cart.remove(productId); // Nếu giảm về 0 thì xóa luôn
            } else {
                cart.get(productId).setQuantity(quantity);
            }
        }
    }

    // 6. Xóa món hàng khỏi giỏ
    public void removeProduct(Long productId) {
        cart.remove(productId);
    }
}