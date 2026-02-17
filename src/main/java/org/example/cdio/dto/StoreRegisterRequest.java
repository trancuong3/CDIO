package org.example.cdio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreRegisterRequest {
    @NotBlank
    private String storeName;

    private String address;

    @NotBlank
    private String representativeName;

    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String fullName;
}
