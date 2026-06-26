package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.Bill;
import com.example.billtrackingsystem.model.Product;
import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import com.example.billtrackingsystem.repository.BillRepository;
import com.example.billtrackingsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@RestController
@RequestMapping("/api/bills")
public class BillApiController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.billtrackingsystem.service.GeminiService geminiService;

    @GetMapping("/summary/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        List<Bill> allBills = billRepository.findByUser(user);

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

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

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Bill> saveBill(@RequestBody Bill bill, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        bill.setUser(user);
        
        // Append _userId to guarantee global uniqueness of PK while allowing duplicate user docNo entries
        String originalDocNo = bill.getDocNo();
        if (originalDocNo != null && !originalDocNo.endsWith("_" + user.getId())) {
            bill.setDocNo(originalDocNo + "_" + user.getId());
        }

        if (bill.getProducts() != null) {
            bill.getProducts().forEach(p -> {
                p.setBill(bill);
                p.setUser(user);
            });
        }
        return ResponseEntity.ok(billRepository.save(bill));
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanReceipt(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String jsonResult = geminiService.scanReceipt(file);
        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/product-lookup/{ian}")
    public ResponseEntity<Product> lookupProduct(@PathVariable String ian, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return productRepository.findTopByIanAndUser(ian, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{docNo}/add-product")
    public ResponseEntity<Bill> addProductToExistingBill(@PathVariable String docNo, @RequestBody Bill updatedData, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        return billRepository.findByDocNoAndUser(docNo, user).map(existingBill -> {
            if (updatedData.getProducts() != null) {
                updatedData.getProducts().forEach(newProd -> {
                    newProd.setBill(existingBill);
                    newProd.setUser(user);
                    existingBill.getProducts().add(newProd);
                });
            }
            return ResponseEntity.ok(billRepository.save(existingBill));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        return productRepository.findById(id).map(product -> {
            if (product.getUser() != null && product.getUser().getId().equals(user.getId())) {
                productRepository.delete(product);
                return ResponseEntity.ok().<Void>build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary/trends")
    public ResponseEntity<Map<String, Object>> getTrends(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Bill> allBills = billRepository.findByUser(user);

        Map<YearMonth, Double> trends = new LinkedHashMap<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            trends.put(currentMonth.minusMonths(i), 0.0);
        }

        for (Bill b : allBills) {
            if (b.getDate() != null) {
                YearMonth ym = YearMonth.from(b.getDate());
                if (trends.containsKey(ym)) {
                    double billTotal = b.getProducts() != null ? b.getProducts().stream()
                            .mapToDouble(p -> p.getPrice() * p.getQuantity()).sum() : 0.0;
                    trends.put(ym, trends.get(ym) + billTotal);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Map.Entry<YearMonth, Double> entry : trends.entrySet()) {
            labels.add(entry.getKey().format(formatter));
            data.add(Math.round(entry.getValue() * 100.0) / 100.0);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/csv")
    public void exportToCsv(HttpServletResponse response, Principal principal) throws Exception {
        if (principal == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Bill> allBills = billRepository.findByUser(user);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"spending_history.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Document No,Date,Store Name,Item Name,Status,Quantity,Unit,Price,Total Item Price");

            for (Bill bill : allBills) {
                String docNo = bill.getDocNo() != null ? bill.getDocNo().replace(",", "") : "";
                String date = bill.getDate() != null ? bill.getDate().toString() : "";
                String storeName = bill.getStoreName() != null ? bill.getStoreName().replace(",", "") : "";

                if (bill.getProducts() != null && !bill.getProducts().isEmpty()) {
                    for (Product p : bill.getProducts()) {
                        String name = p.getName() != null ? p.getName().replace(",", "") : "";
                        String status = p.getStatus() != null ? p.getStatus().replace(",", "") : "IN_STOCK";
                        double qty = p.getQuantity();
                        String unit = p.getUnit() != null ? p.getUnit().replace(",", "") : "";
                        double price = p.getPrice();
                        double total = qty * price;
                        writer.printf("%s,%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%n", docNo, date, storeName, name, status, qty, unit, price, total);
                    }
                } else {
                    writer.printf("%s,%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%n", docNo, date, storeName, "", "", 0.0, "", 0.0, 0.0);
                }
            }
        }
    }

    @GetMapping("/summary/shops")
    public ResponseEntity<Map<String, Object>> getShopSummary(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Bill> allBills = billRepository.findByUser(user);

        Map<String, Double> shopSales = new HashMap<>();
        for (Bill bill : allBills) {
            String shop = bill.getStoreName() != null && !bill.getStoreName().trim().isEmpty() ? bill.getStoreName().trim() : "Unknown";
            double total = bill.getTotalAmount();
            shopSales.put(shop, shopSales.getOrDefault(shop, 0.0) + total);
        }

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        for (Map.Entry<String, Double> entry : shopSales.entrySet()) {
            labels.add(entry.getKey());
            data.add(entry.getValue());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/products/{id}/status")
    public ResponseEntity<?> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Product product = productRepository.findById(id).orElseThrow();
        if (!product.getUser().getId().equals(user.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        product.setStatus(payload.get("status"));
        productRepository.save(product);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{docNo}/status")
    public ResponseEntity<?> updateBillStatus(@PathVariable String docNo, @RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Bill bill = billRepository.findByDocNoAndUser(docNo, user).orElseThrow();

        String newStatus = payload.get("status");
        if (bill.getProducts() != null) {
            for (Product p : bill.getProducts()) {
                p.setStatus(newStatus);
            }
            billRepository.save(bill);
        }
        return ResponseEntity.ok().build();
    }
}