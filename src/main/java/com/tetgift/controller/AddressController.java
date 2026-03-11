package com.tetgift.controller;

import com.tetgift.dto.request.AddressRequest;
import com.tetgift.dto.response.AddressResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Address Management", description = "APIs for managing user addresses")
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Create address", description = "Create a new delivery address")
    public ResponseEntity<ResponseData<AddressResponse>> createAddress(@RequestBody @Valid AddressRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseData<>(
                        HttpStatus.CREATED.value(),
                        "Address created successfully",
                        addressService.createAddress(request)));
    }

    @GetMapping
    @Operation(summary = "Get my addresses", description = "Get all addresses of the current user")
    public ResponseEntity<ResponseData<List<AddressResponse>>> getMyAddresses() {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Addresses fetched successfully",
                addressService.getMyAddresses()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Get address details by its ID")
    public ResponseEntity<ResponseData<AddressResponse>> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Address fetched successfully",
                addressService.getAddressById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Update an existing address")
    public ResponseEntity<ResponseData<AddressResponse>> updateAddress(@PathVariable Long id,
            @RequestBody @Valid AddressRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Address updated successfully",
                addressService.updateAddress(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Delete an address")
    public ResponseEntity<ResponseData<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Address deleted successfully",
                null));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set default address", description = "Set an address as the default delivery address")
    public ResponseEntity<ResponseData<AddressResponse>> setDefaultAddress(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Default address set successfully",
                addressService.setDefaultAddress(id)));
    }
}
