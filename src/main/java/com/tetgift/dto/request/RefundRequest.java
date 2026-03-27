package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequest {
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    private String bankAccount;

    @NotBlank(message = "Account holder name is required")
    private String accountHolder;
}
