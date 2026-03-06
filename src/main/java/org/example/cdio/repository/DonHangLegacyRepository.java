package org.example.cdio.repository;

import org.example.cdio.entity.DonHangLegacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DonHangLegacyRepository extends JpaRepository<DonHangLegacy, String> {

	@Query(value = """
			select
				dh.id as id,
				dh.ma_don_hang as maDonHang,
				dh.ngay_dat as ngayDat,
				dh.trang_thai as trangThai,
				dh.sdt as sdt,
				dh.tong_tien as tongTien
			from don_hangs dh
			order by dh.ngay_dat desc
			""", nativeQuery = true)
	List<DonHangLegacyProjection> findRecentLegacyOrders();

	@Query(value = """
			select
				dh.id as id,
				dh.ma_don_hang as maDonHang,
				dh.ngay_dat as ngayDat,
				dh.trang_thai as trangThai,
				dh.sdt as sdt,
				dh.tong_tien as tongTien
			from don_hangs dh
			where dh.ma_don_hang = :maDonHang
			""", nativeQuery = true)
	Optional<DonHangLegacyProjection> findLegacyOrderById(@Param("maDonHang") String maDonHang);

	@Modifying
	@Transactional
	@Query(value = """
			update don_hangs
			set trang_thai = :trangThai
			where ma_don_hang = :maDonHang
			""", nativeQuery = true)
	int updateLegacyStatus(@Param("maDonHang") String maDonHang, @Param("trangThai") String trangThai);
}
