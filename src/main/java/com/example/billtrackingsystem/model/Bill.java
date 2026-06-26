package com.example.billtrackingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "bill")
@Getter @Setter
public class Bill {
    @Id
    private String docNo;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String storeName;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public String getDisplayDocNo() {
        if (docNo != null && docNo.contains("_")) {
            return docNo.substring(0, docNo.lastIndexOf("_"));
        }
        return docNo;
    }

    public Double getTotalAmount() {
        if (products == null || products.isEmpty()) return 0.0;
        return products.stream()
                .mapToDouble(p -> p.getPrice() != null && p.getQuantity() != null ? p.getPrice() * p.getQuantity() : 0.0)
                .sum();
    }

    public String getOverallStatus() {
        if (products == null || products.isEmpty()) return "IN_STOCK";
        boolean allSold = products.stream().allMatch(p -> "SOLD".equals(p.getStatus()));
        boolean allInStock = products.stream().allMatch(p -> "IN_STOCK".equals(p.getStatus()));
        if (allSold) return "SOLD";
        if (allInStock) return "IN_STOCK";
        return "MIXED";
    }
}