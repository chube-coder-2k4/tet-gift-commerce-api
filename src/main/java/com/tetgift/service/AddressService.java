package com.tetgift.service;

import com.tetgift.dto.request.AddressRequest;
import com.tetgift.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(AddressRequest request);

    List<AddressResponse> getMyAddresses();

    AddressResponse getAddressById(Long id);

    AddressResponse updateAddress(Long id, AddressRequest request);

    void deleteAddress(Long id);

    AddressResponse setDefaultAddress(Long id);
}
