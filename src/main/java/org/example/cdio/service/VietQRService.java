package org.example.cdio.service;

import org.springframework.stereotype.Service;

@Service
public class VietQRService {

    private static final String BANK = "MB"; // ngân hàng
    private static final String ACCOUNT = "0905373633"; // stk của bạn

    public String generateQR(Long orderId, Double amount){

        String note = "ORDER" + orderId;

        return "https://img.vietqr.io/image/"
                + BANK + "-"
                + ACCOUNT
                + "-compact.png?amount="
                + amount.intValue()
                + "&addInfo="
                + note;
    }
}