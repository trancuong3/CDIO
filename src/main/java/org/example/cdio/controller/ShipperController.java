package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Delivery;
import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderItem;
import org.example.cdio.entity.OrderStatus;
import org.example.cdio.entity.Store;
import org.example.cdio.entity.User;
import org.example.cdio.repository.DeliveryRepository;
import org.example.cdio.repository.DonHangLegacyProjection;
import org.example.cdio.repository.DonHangLegacyRepository;
import org.example.cdio.repository.OrderItemRepository;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shipper")
public class ShipperController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final DonHangLegacyRepository donHangLegacyRepository;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "deliveryId", required = false) Long deliveryId,
            Principal principal,
            Model model
    ) {
        User shipper = resolveCurrentUser(principal);
        model.addAttribute("shipperName", shipper.getFullName() != null ? shipper.getFullName() : shipper.getUsername());
        model.addAttribute("shipperId", shipper.getId());

        List<Order> pendingOrders = orderRepository.findByStatusWithStoreOrderByCreatedAtDesc(OrderStatus.APPROVED);
        List<Order> deliveringOrders = orderRepository.findByCreatedByAndStatusWithStoreOrderByCreatedAtDesc(
                shipper.getId(),
                OrderStatus.DELIVERING
        );
        List<Delivery> deliveryHistory = deliveryRepository.findByDeliveredByOrderByCreatedAtDesc(shipper.getId());
        List<DonHangLegacyProjection> legacyOrders = readLegacyOrdersSafe();
        Map<String, String> legacyStatusByOrder = buildLegacyStatusMap(shipper.getId());
        Set<String> legacyOwnedOrderIds = new HashSet<>(legacyStatusByOrder.keySet());

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("deliveryHistory", deliveryHistory);
        model.addAttribute("legacyOrders", legacyOrders);
        model.addAttribute("legacyStatusByOrder", legacyStatusByOrder);
        model.addAttribute("legacyOwnedOrderIds", legacyOwnedOrderIds);
        model.addAttribute("deliveryStatusOptions", List.of("DELIVERING", "DELIVERED", "REJECTED", "CANCELLED"));

        Delivery selectedDelivery = resolveSelectedDelivery(deliveryHistory, deliveryId);
        model.addAttribute("selectedDelivery", selectedDelivery);

        Set<String> orderCodeOptions = new LinkedHashSet<>(
            legacyOrders.stream()
                .map(DonHangLegacyProjection::getMaDonHang)
                .filter(code -> code != null && !code.isBlank())
                .toList()
        );
        if (selectedDelivery != null && selectedDelivery.getMaDonHang() != null && !selectedDelivery.getMaDonHang().isBlank()) {
            orderCodeOptions.add(selectedDelivery.getMaDonHang());
        }
        model.addAttribute("orderCodeOptions", orderCodeOptions.stream().toList());

        Map<String, Long> legacyIdByCode = legacyOrders.stream()
            .filter(o -> o.getMaDonHang() != null && !o.getMaDonHang().isBlank() && o.getId() != null)
            .collect(Collectors.toMap(DonHangLegacyProjection::getMaDonHang, DonHangLegacyProjection::getId, (a, b) -> a));

        if (selectedDelivery != null
                && selectedDelivery.getMaDonHang() != null
                && !selectedDelivery.getMaDonHang().isBlank()
                && !legacyIdByCode.containsKey(selectedDelivery.getMaDonHang())) {
            DonHangLegacyProjection selectedLegacy = findLegacyOrderSafe(selectedDelivery.getMaDonHang());
            if (selectedLegacy != null && selectedLegacy.getId() != null) {
                legacyIdByCode.put(selectedLegacy.getMaDonHang(), selectedLegacy.getId());
            }
        }

        model.addAttribute("legacyIdByCode", legacyIdByCode);
        Long selectedLegacyId = selectedDelivery != null ? legacyIdByCode.get(selectedDelivery.getMaDonHang()) : null;
        model.addAttribute("selectedLegacyId", selectedLegacyId);

        Long selectedOrderId = resolveSelectedOrderId(orderId, deliveringOrders);
        if (selectedOrderId == null) {
            model.addAttribute("orderItems", Collections.emptyList());
            model.addAttribute("orderStatuses", List.of(OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.CANCELLED));
            return "shipper/dashboard";
        }

        List<Delivery> deliveryRows = deliveryRepository.findByOrderIdAndDeliveredByOrderByIdAsc(selectedOrderId, shipper.getId());
        List<OrderLineView> orderLines = toOrderLinesFromDeliveries(deliveryRows);

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithDetails(selectedOrderId);
        if (orderItems.isEmpty() && orderLines.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            model.addAttribute("orderLines", Collections.emptyList());
            model.addAttribute("orderStatuses", List.of(OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.CANCELLED));
            return "shipper/dashboard";
        }

        Order order = !orderItems.isEmpty()
            ? orderItems.get(0).getOrder()
            : orderRepository.findById(selectedOrderId).orElse(null);

        if (order == null) {
            model.addAttribute("orderItems", Collections.emptyList());
            model.addAttribute("orderLines", orderLines);
            model.addAttribute("orderStatuses", List.of(OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.CANCELLED));
            return "shipper/dashboard";
        }

        Store store = order.getStore();
        Delivery latestDelivery = deliveryRows.stream()
            .filter(d -> d.getCreatedAt() != null)
            .max(Comparator.comparing(Delivery::getCreatedAt))
            .orElseGet(() -> deliveryRows.stream().findFirst().orElse(null));

        model.addAttribute("orderItems", orderItems);
        model.addAttribute("orderLines", orderLines);
        model.addAttribute("orderId", order.getId());
        model.addAttribute("deliverySlipCode", "PXG-" + order.getId());
        model.addAttribute("orderCode", order.getId());
        model.addAttribute("createdDate", latestDelivery != null && latestDelivery.getCreatedAt() != null
            ? latestDelivery.getCreatedAt()
            : order.getCreatedAt());
        model.addAttribute("deliveryStatus", resolveStatus(latestDelivery, order));
        model.addAttribute("incident", latestDelivery != null ? latestDelivery.getIncident() : order.getRejectedReason());
        model.addAttribute("orderTotal", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);

        if (store != null) {
            model.addAttribute("customerName", store.getRepresentativeName() != null ? store.getRepresentativeName() : store.getName());
            model.addAttribute("customerPhone", order.getSdt() != null ? order.getSdt() : store.getPhone());
            model.addAttribute("deliveryAddress", store.getAddress());
        }

        model.addAttribute("orderStatuses", List.of(OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        return "shipper/dashboard";
    }

    @PostMapping("/orders/{orderId}/accept")
    public String acceptOrder(
            @PathVariable Long orderId,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        User shipper = resolveCurrentUser(principal);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang."));

        if (order.getStatus() != OrderStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Don hang da duoc xu ly boi nguoi khac.");
            return "redirect:/shipper/dashboard";
        }

        order.setCreatedBy(shipper.getId());
        order.setStatus(OrderStatus.DELIVERING);
        orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithDetails(orderId);
        LocalDateTime now = LocalDateTime.now();
        for (OrderItem item : orderItems) {
            Delivery delivery = new Delivery();
            delivery.setMaDonHang(String.valueOf(order.getId()));
            delivery.setNgayDat(now.toLocalDate());
            delivery.setTrangThai(OrderStatus.DELIVERING.name());
            delivery.setSdt(order.getSdt());
            delivery.setOrderId(order.getId());
            delivery.setDelivererName(shipper.getFullName() != null ? shipper.getFullName() : shipper.getUsername());
            delivery.setShippedAt(now);
            delivery.setStatus(OrderStatus.DELIVERING.name());
            delivery.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
            delivery.setQuantity(item.getQuantity());
            delivery.setUnitPrice(item.getUnitPrice());
            delivery.setLineTotal(item.getLineTotal());
            delivery.setDeliveredBy(shipper.getId());
            delivery.setCreatedAt(now);
            deliveryRepository.save(delivery);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Nhan don thanh cong.");
        return "redirect:/shipper/dashboard?orderId=" + orderId;
    }

    @PostMapping("/delivery/update")
    public String updateDelivery(
            @RequestParam("orderId") Long orderId,
            @RequestParam("deliveryDate") LocalDate deliveryDate,
            @RequestParam("deliveryStatus") OrderStatus deliveryStatus,
            @RequestParam(value = "incident", required = false) String incident,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        User shipper = resolveCurrentUser(principal);
        Order order = orderRepository.findByIdAndCreatedBy(orderId, shipper.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ban khong co quyen cap nhat don nay."));

        LocalDateTime base = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
        order.setCreatedAt(LocalDateTime.of(deliveryDate, base.toLocalTime()));
        order.setStatus(deliveryStatus);
        order.setRejectedReason(incident);
        orderRepository.save(order);

        List<Delivery> deliveries = deliveryRepository.findByOrderIdAndDeliveredByOrderByIdAsc(orderId, shipper.getId());
        for (Delivery delivery : deliveries) {
            delivery.setNgayDat(deliveryDate);
            delivery.setTrangThai(deliveryStatus.name());
            delivery.setIncident(incident);
            delivery.setCreatedAt(LocalDateTime.now());
        }
        deliveryRepository.saveAll(deliveries);

        redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat trang thai giao hang.");
        return "redirect:/shipper/dashboard?orderId=" + orderId;
    }

    @PostMapping("/legacy/{maDonHang}/accept")
    public String acceptLegacyOrder(
            @PathVariable String maDonHang,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        User shipper = resolveCurrentUser(principal);
        DonHangLegacyProjection legacyOrder = donHangLegacyRepository.findLegacyOrderById(maDonHang)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang legacy."));

        if ("DELIVERING".equalsIgnoreCase(legacyOrder.getTrangThai())
                || "DELIVERED".equalsIgnoreCase(legacyOrder.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Don hang nay da duoc shipper khac xu ly.");
            return "redirect:/shipper/dashboard";
        }

        int updated = donHangLegacyRepository.updateLegacyStatus(maDonHang, "DELIVERING");
        if (updated == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong cap nhat duoc trang thai don legacy.");
            return "redirect:/shipper/dashboard";
        }

        Delivery delivery = new Delivery();
        delivery.setMaDonHang(maDonHang);
        delivery.setOrderId(resolveOrderIdForLegacyInsert(maDonHang));
        delivery.setNgayDat(LocalDate.now());
        delivery.setTrangThai("DELIVERING");
        delivery.setSdt(legacyOrder.getSdt());
        delivery.setUnitPrice(legacyOrder.getTongTien());
        delivery.setLineTotal(legacyOrder.getTongTien());
        delivery.setDelivererName(shipper.getFullName() != null ? shipper.getFullName() : shipper.getUsername());
        delivery.setShippedAt(LocalDateTime.now());
        delivery.setStatus("DELIVERING");
        delivery.setDeliveredBy(shipper.getId());
        delivery.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        redirectAttributes.addFlashAttribute("successMessage", "Da nhan giao don legacy #" + maDonHang + ".");
        return "redirect:/shipper/dashboard?deliveryId=" + delivery.getId();
    }

    @PostMapping("/legacy/{maDonHang}/confirm")
    public String confirmLegacyDelivered(
            @PathVariable String maDonHang,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        User shipper = resolveCurrentUser(principal);

        Delivery assignedDelivery = deliveryRepository
                .findFirstByMaDonHangAndDeliveredByOrderByCreatedAtDesc(maDonHang, shipper.getId())
                .orElse(null);

        if (assignedDelivery == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ban chua nhan don legacy nay.");
            return "redirect:/shipper/dashboard";
        }

        int updated = donHangLegacyRepository.updateLegacyStatus(maDonHang, "DELIVERED");
        if (updated == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong cap nhat duoc trang thai giao xong.");
            return "redirect:/shipper/dashboard";
        }

        Delivery confirmRow = new Delivery();
        confirmRow.setMaDonHang(maDonHang);
        confirmRow.setOrderId(resolveOrderIdForLegacyInsert(maDonHang));
        confirmRow.setNgayDat(LocalDate.now());
        confirmRow.setTrangThai("DELIVERED");
        confirmRow.setSdt(assignedDelivery.getSdt());
        confirmRow.setUnitPrice(assignedDelivery.getUnitPrice());
        confirmRow.setLineTotal(assignedDelivery.getLineTotal());
        confirmRow.setDelivererName(assignedDelivery.getDelivererName());
        confirmRow.setShippedAt(assignedDelivery.getShippedAt());
        confirmRow.setDeliveredAt(LocalDateTime.now());
        confirmRow.setStatus("DELIVERED");
        confirmRow.setIssueNote(assignedDelivery.getIssueNote());
        confirmRow.setDeliveredBy(shipper.getId());
        confirmRow.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(confirmRow);

        redirectAttributes.addFlashAttribute("successMessage", "Da xac nhan giao thanh cong don legacy #" + maDonHang + ".");
        return "redirect:/shipper/dashboard";
    }

    @PostMapping("/deliveries/{deliveryId}/update")
    public String updateLegacyDelivery(
            @PathVariable Long deliveryId,
            @RequestParam("maDonHang") String maDonHang,
            @RequestParam("trangThai") String trangThai,
            @RequestParam("ngayDat") LocalDate ngayDat,
            @RequestParam(value = "sdt", required = false) String sdt,
            @RequestParam(value = "incident", required = false) String incident,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        User shipper = resolveCurrentUser(principal);
        Delivery delivery = deliveryRepository.findByIdAndDeliveredBy(deliveryId, shipper.getId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phieu giao cua ban."));

        DonHangLegacyProjection legacyOrder = findLegacyOrderSafe(maDonHang);

        delivery.setMaDonHang(maDonHang);
        // Keep existing order_id to satisfy FK; legacy flow only syncs deliveries <-> don_hangs.
        delivery.setTrangThai(trangThai);
        delivery.setNgayDat(ngayDat);
        delivery.setSdt(sdt);
        if (legacyOrder != null && legacyOrder.getTongTien() != null) {
            delivery.setUnitPrice(legacyOrder.getTongTien());
            delivery.setLineTotal(legacyOrder.getTongTien());
        }
        delivery.setIncident(incident);
        delivery.setStatus(trangThai);
        delivery.setIssueNote(incident);
        if (delivery.getDelivererName() == null || delivery.getDelivererName().isBlank()) {
            delivery.setDelivererName(shipper.getFullName() != null ? shipper.getFullName() : shipper.getUsername());
        }
        if ("DELIVERING".equalsIgnoreCase(trangThai) && delivery.getShippedAt() == null) {
            delivery.setShippedAt(LocalDateTime.now());
        }
        if ("DELIVERED".equalsIgnoreCase(trangThai)) {
            delivery.setDeliveredAt(LocalDateTime.now());
        }
        delivery.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        if (maDonHang != null && !maDonHang.isBlank()) {
            donHangLegacyRepository.updateLegacyStatus(maDonHang, trangThai);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat thong tin giao hang.");
        return "redirect:/shipper/dashboard?deliveryId=" + deliveryId;
    }

    private DonHangLegacyProjection findLegacyOrderSafe(String maDonHang) {
        try {
            return donHangLegacyRepository.findLegacyOrderById(maDonHang).orElse(null);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private List<DonHangLegacyProjection> readLegacyOrdersSafe() {
        try {
            return donHangLegacyRepository.findRecentLegacyOrders();
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    private Delivery resolveSelectedDelivery(List<Delivery> deliveryHistory, Long deliveryId) {
        if (deliveryHistory == null || deliveryHistory.isEmpty()) {
            return null;
        }

        if (deliveryId == null) {
            return deliveryHistory.get(0);
        }

        return deliveryHistory.stream()
                .filter(d -> d.getId() != null && d.getId().equals(deliveryId))
                .findFirst()
                .orElse(deliveryHistory.get(0));
    }

    private Long resolveOrderIdForLegacyInsert(String maDonHang) {
        return deliveryRepository.findFirstByMaDonHangOrderByCreatedAtDesc(maDonHang)
                .map(Delivery::getOrderId)
                .filter(id -> id != null)
                .orElseGet(() -> orderRepository.findAll().stream().map(Order::getId).findFirst().orElse(1L));
    }

    private Map<String, String> buildLegacyStatusMap(Long shipperId) {
        List<Delivery> legacyRows = deliveryRepository.findByDeliveredByAndMaDonHangIsNotNullOrderByCreatedAtDesc(shipperId);
        Map<String, String> map = new HashMap<>();
        for (Delivery row : legacyRows) {
            if (row.getMaDonHang() == null || map.containsKey(row.getMaDonHang())) {
                continue;
            }
            map.put(row.getMaDonHang(), row.getTrangThai());
        }
        return map;
    }

    private List<OrderLineView> toOrderLinesFromDeliveries(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderLineView> lines = new ArrayList<>();
        for (Delivery d : deliveries) {
            lines.add(new OrderLineView(
                    d.getProductId(),
                    d.getQuantity(),
                    d.getUnitPrice(),
                    d.getLineTotal()
            ));
        }
        return lines.stream()
                .filter(l -> l.quantity() != null || l.unitPrice() != null || l.lineTotal() != null)
                .collect(Collectors.toList());
    }

    private OrderStatus resolveStatus(Delivery latestDelivery, Order order) {
        if (latestDelivery != null && latestDelivery.getTrangThai() != null) {
            try {
                return OrderStatus.valueOf(latestDelivery.getTrangThai());
            } catch (IllegalArgumentException ignored) {
                // Keep fallback behavior when legacy data has unknown status values.
            }
        }
        return order.getStatus();
    }

    private record OrderLineView(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }

    private User resolveCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Phien dang nhap khong hop le.");
        }

        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan shipper."));
    }

    private Long resolveSelectedOrderId(Long requestedOrderId, List<Order> deliveringOrders) {
        if (requestedOrderId != null) {
            return requestedOrderId;
        }

        if (deliveringOrders.isEmpty()) {
            return null;
        }

        return deliveringOrders.get(0).getId();
    }
}
