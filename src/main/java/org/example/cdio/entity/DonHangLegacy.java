package org.example.cdio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "don_hangs")
public class DonHangLegacy {

	@Id
	@Column(name = "ma_don_hang")
	private String maDonHang;

	public String getMaDonHang() {
		return maDonHang;
	}

	public void setMaDonHang(String maDonHang) {
		this.maDonHang = maDonHang;
	}
}
