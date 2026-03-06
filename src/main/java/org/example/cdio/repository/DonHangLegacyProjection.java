package org.example.cdio.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DonHangLegacyProjection {
	Long getId();
	String getMaDonHang();
	LocalDateTime getNgayDat();
	String getTrangThai();
	String getSdt();
	BigDecimal getTongTien();
}
