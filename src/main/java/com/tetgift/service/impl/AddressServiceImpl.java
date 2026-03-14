package com.tetgift.service.impl;

import com.tetgift.dto.request.AddressRequest;
import com.tetgift.dto.response.AddressResponse;
import com.tetgift.exception.ForBiddenException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Address;
import com.tetgift.model.Users;
import com.tetgift.repository.jpa.AddressRepository;
import com.tetgift.service.AddressService;
import com.tetgift.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AuthenticationUtils authenticationUtils;

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        Users currentUser = authenticationUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ForBiddenException("User not authenticated");
        }

        Address address = Address.builder()
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .addressDetail(request.getAddressDetail())
                .isDefault(request.isDefault())
                .user(currentUser)
                .build();

        // If this is set as default, clear other defaults
        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(currentUser.getId(), -1L);
        }

        Address saved = addressRepository.save(address);
        return toResponse(saved);
    }

    @Override
    public List<AddressResponse> getMyAddresses() {
        Users currentUser = authenticationUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ForBiddenException("User not authenticated");
        }

        return addressRepository.findByUserIdOrderByIsDefaultDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AddressResponse getAddressById(Long id) {
        Address address = findAndValidateOwnership(id);
        return toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = findAndValidateOwnership(id);

        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setAddressDetail(request.getAddressDetail());
        address.setDefault(request.isDefault());

        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(address.getUser().getId(), id);
        }

        Address updated = addressRepository.save(address);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        Address address = findAndValidateOwnership(id);
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long id) {
        Address address = findAndValidateOwnership(id);

        addressRepository.clearDefaultForUser(address.getUser().getId(), id);
        address.setDefault(true);
        Address updated = addressRepository.save(address);

        return toResponse(updated);
    }

    private Address findAndValidateOwnership(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        Users currentUser = authenticationUtils.getCurrentUser();
        if (currentUser == null || !address.getUser().getId().equals(currentUser.getId())) {
            throw new ForBiddenException("You do not have permission to access this address");
        }

        return address;
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .addressDetail(address.getAddressDetail())
                .isDefault(address.isDefault())
                .build();
    }
}
