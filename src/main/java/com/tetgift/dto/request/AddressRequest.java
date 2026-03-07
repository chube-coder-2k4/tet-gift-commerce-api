package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address detail is required")
    private String addressDetail;

    private boolean isDefault = false;
}
