package com.tetgift.dto.response;

import java.io.Serializable;

public class PageResponse<T> implements Serializable {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private T items;
}
