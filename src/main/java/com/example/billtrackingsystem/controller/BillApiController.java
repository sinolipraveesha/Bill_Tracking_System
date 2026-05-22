package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.Bill;
import com.example.billtrackingsystem.model.Product;
import com.example.billtrackingsystem.repository.BillRepository;
import com.example.billtrackingsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillApiController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/summary/monthly")
    public Map<String, Object> getMonthlySummary() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        List<Bill> allBills = billRepository.findAll();

        double monthlySpend = allBills.stream()
                .filter(b -> b.getDate() != null && !b.getDate().isBefore(start) && !b.getDate().isAfter(end))
                .flatMap(b -> b.getProducts().stream())
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();

        long monthlyCount = allBills.stream()
                .filter(b -> b.getDate() != null && !b.getDate().isBefore(start) && !b.getDate().isAfter(end))
                .count();

        double totalSpendAllTime = allBills.stream()
                .flatMap(b -> b.getProducts().stream())
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("monthlySpend", monthlySpend);
        response.put("monthlyCount", monthlyCount);
        response.put("totalSpendAllTime", totalSpendAllTime);
        response.put("totalBillCount", (long) allBills.size());
        response.put("monthName", now.getMonth().name());

        return response;
    }

    @PostMapping
    public Bill saveBill(@RequestBody Bill bill) {
        if (bill.getProducts() != null) {
            bill.getProducts().forEach(p -> p.setBill(bill));
        }
        return billRepository.save(bill);
    }

    @GetMapping("/product-lookup/{ian}")
    public ResponseEntity<Product> lookupProduct(@PathVariable String ian) {
        return productRepository.findTopByIanOrderByIdDesc(ian)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{docNo}/add-product")
    public Bill addProductToExistingBill(@PathVariable String docNo, @RequestBody Bill updatedData) {
        return billRepository.findById(docNo).map(existingBill -> {
            if (updatedData.getProducts() != null) {
                updatedData.getProducts().forEach(newProd -> {
                    newProd.setBill(existingBill);
                    existingBill.getProducts().add(newProd);
                });
            }
            return billRepository.save(existingBill);
        }).orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    @DeleteMapping("/product/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}