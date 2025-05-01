package com.backend.solution.feature.product.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductDetail {
    @Size(min = 1)
    private String id;
    @Size(min = 1)
    private String name;
    private int price;
    private boolean availability;
}
