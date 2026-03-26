package com.tetgift.service;

import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.TopCustomerResponse;

public interface StatisticService {
    PageResponse<TopCustomerResponse> getTopCustomers(int page, int size);
}
