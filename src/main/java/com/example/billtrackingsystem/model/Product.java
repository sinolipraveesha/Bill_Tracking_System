package com.example.billtrackingsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter @Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ian_number",nullable = true)

    private String ian;
    private String name;
    private Double price;
    private Double quantity; // Check if this matches your DB
    private String unit;
    private String status = "IN_STOCK";

    @ManyToOne
    @JoinColumn(name = "bill_doc_no")
    @JsonIgnore
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // Standard getter logic to prevent NullPointerException
    public Double getPrice() { return price == null ? 0.0 : price; }
    public Double getQuantity() { return quantity == null ? 0.0 : quantity; }
}