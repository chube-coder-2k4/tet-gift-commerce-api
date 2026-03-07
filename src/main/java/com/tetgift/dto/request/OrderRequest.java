package com.tetgift.dto.request;

import lombok.Data;

@Data
public class OrderRequest {
    private Long addressId;
    private String discountCode;

    // VAT info (optional)
    private String vatCompanyName;
    private String vatTaxCode;
    private String vatPhone;
    private String vatAddress;
}
