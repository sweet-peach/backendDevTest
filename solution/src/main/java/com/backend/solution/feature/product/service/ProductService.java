package com.backend.solution.feature.product.service;

import com.backend.solution.feature.product.dto.ProductDetail;

import java.util.List;

public interface ProductService {
    List<ProductDetail> getSimilarProducts(String id);
}
