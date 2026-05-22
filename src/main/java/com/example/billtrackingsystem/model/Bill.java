package com.example.billtrackingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter
public class Bill {
    @Id
    private String docNo;
    private LocalDate date;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    public Double getTotalAmount() {
        if (products == null || products.isEmpty()) return 0.0;
        return products.stream()
                .mapToDouble(p -> p.getPrice() != null && p.getQuantity() != null ? p.getPrice() * p.getQuantity() : 0.0)
                .sum();
    }
}