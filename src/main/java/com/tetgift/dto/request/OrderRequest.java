package com.tetgift.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull(message = "Address ID is required")
    private Long addressId;

    private String discountCode;

    // VAT info (optional)
    private String vatCompanyName;
    private String vatTaxCode;

    @Pattern(regexp = "(84|0[35789])+([0-9]{8})\\b", message = "VAT phone number is invalid")
    private String vatPhone;

    private String vatAddress;
}
