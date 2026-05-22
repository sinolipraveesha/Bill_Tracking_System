package com.example.billtrackingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResult {
    private String ian;      // Changed from ianNumber
    private Double price;
    private String docNo;
    private LocalDate date;
}