package com.tetgift.service.impl;

import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.TopCustomerResponse;
import com.tetgift.repository.jpa.OrderRepository;
import com.tetgift.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final OrderRepository orderRepository;

    @Override
    public PageResponse<TopCustomerResponse> getTopCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        Page<TopCustomerResponse> topCustomers = orderRepository.findTopCustomers(pageable);
        
        return PageResponse.<TopCustomerResponse>builder()
                .data(topCustomers.getContent())
                .pageNo(topCustomers.getNumber())
                .pageSize(topCustomers.getSize())
                .totalItems(topCustomers.getTotalElements())
                .totalPages(topCustomers.getTotalPages())
                .build();
    }
}
