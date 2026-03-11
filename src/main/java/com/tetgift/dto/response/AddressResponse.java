package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String phone;
    private String addressDetail;
    private boolean isDefault;
}
