package com.tetgift.controller;

import com.tetgift.dto.request.DiscountRequest;
import com.tetgift.dto.response.DiscountResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discounts")
@Tag(name = "Discount Management", description = "APIs for managing discount codes")
public class DiscountController {
    private final DiscountService discountService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create discount (ADMIN)")
    public ResponseEntity<ResponseData<DiscountResponse>> create(@RequestBody @Valid DiscountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Discount created",
                        discountService.createDiscount(request)));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all discounts (ADMIN)")
    public ResponseEntity<ResponseData<List<DiscountResponse>>> getAll() {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Discounts fetched", discountService.getAllDiscounts()));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get discount by ID")
    public ResponseEntity<ResponseData<DiscountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Discount fetched", discountService.getDiscountById(id)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update discount (ADMIN)")
    public ResponseEntity<ResponseData<DiscountResponse>> update(@PathVariable Long id,
            @RequestBody @Valid DiscountRequest request) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Discount updated",
                discountService.updateDiscount(id, request)));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete discount (ADMIN)")
    public ResponseEntity<ResponseData<Void>> delete(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Discount deleted", null));
    }

    @PostMapping("/validate")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Validate discount code (USER)")
    public ResponseEntity<ResponseData<DiscountResponse>> validate(@RequestParam String code,@RequestParam BigDecimal orderAmount ) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Discount code is valid",
                discountService.validateDiscountCode(code, orderAmount)));
    }
}
