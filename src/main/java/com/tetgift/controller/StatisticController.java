package com.tetgift.controller;

import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.TopCustomerResponse;
import com.tetgift.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/top-customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Top Customers", description = "Retrieve a paginated list of top customers based on total spent amount and order count")
    public ResponseEntity<ResponseData<PageResponse<TopCustomerResponse>>> getTopCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        PageResponse<TopCustomerResponse> topCustomers = statisticService.getTopCustomers(page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Top customers fetched successfully",
                        topCustomers
                ));
    }
}
