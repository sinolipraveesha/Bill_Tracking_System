package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.Bill;
import com.example.billtrackingsystem.model.Product;
import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.BillRepository;
import com.example.billtrackingsystem.repository.ProductRepository;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class BillController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. MAIN LIST & SEARCH BY DOC NO
    @GetMapping("/all-bills")
    public String viewAllBills(@RequestParam(required = false) String docNo, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        List<Bill> bills;
        if (docNo != null && !docNo.isEmpty()) {
            bills = billRepository.findByDocNoContainingAndUser(docNo, user);
        } else {
            bills = billRepository.findByUser(user);
        }

        model.addAttribute("bills", bills);
        return "view-bills";
    }

    // 2. VIEW SPECIFIC BILL DETAILS
    @GetMapping("/view-bill/{docNo}")
    public String viewBillDetails(@PathVariable String docNo, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Optional<Bill> bill = billRepository.findByDocNoAndUser(docNo, user);
        if (bill.isPresent()) {
            model.addAttribute("bill", bill.get());
            return "bill-details";
        }
        return "redirect:/all-bills";
    }

    // 3. DELETE A BILL
    @PostMapping("/delete-bill/{docNo}")
    public String deleteBill(@PathVariable String docNo, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        billRepository.findByDocNoAndUser(docNo, user).ifPresent(bill -> {
            billRepository.delete(bill);
        });
        return "redirect:/all-bills";
    }

    // 4. SEARCH BY IAN NUMBER
    @GetMapping("/search-ian")
    public String searchByIan(@RequestParam(required = false) String ian, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        if (ian != null && !ian.isEmpty()) {
            List<Product> products = productRepository.findProductDetailsByIanAndUser(ian, user);
            model.addAttribute("results", products);
            model.addAttribute("searchedIan", ian);
        }
        return "ian-search-results";
    }

    @GetMapping("/export-pdf/{docNo}")
    public void exportToPDF(@PathVariable String docNo, HttpServletResponse response, Principal principal) throws IOException {
        if (principal == null) {
            return;
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Optional<Bill> optBill = billRepository.findByDocNoAndUser(docNo, user);
        if (optBill.isEmpty()) return;
        Bill bill = optBill.get();

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bill_" + bill.getDisplayDocNo() + ".pdf";
        response.setHeader(headerKey, headerValue);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);

        Paragraph title = new Paragraph("INVOICE - " + bill.getDisplayDocNo(), fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Date: " + bill.getDate()));
        document.add(new Paragraph(" ")); // Spacer

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell("IAN Number");
        table.addCell("Product Name");
        table.addCell("Price (EUR)");

        for (Product product : bill.getProducts()) {
            table.addCell(product.getIan());
            table.addCell(product.getName());
            table.addCell(String.valueOf(product.getPrice()));
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Amount: EUR " + bill.getTotalAmount()));

        document.close();
    }


    // --- NEW: SHOW UPDATE FORM ---
    @GetMapping("/edit-bill/{docNo}")
    public String showUpdateForm(@PathVariable String docNo, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Optional<Bill> bill = billRepository.findByDocNoAndUser(docNo, user);
        if (bill.isPresent()) {
            model.addAttribute("bill", bill.get());
            return "update-bill";
        }
        return "redirect:/all-bills";
    }

    // --- NEW: PROCESS UPDATE ---
    @PostMapping("/update-bill/{docNo}")
    public String updateBill(@PathVariable String docNo, @ModelAttribute("bill") Bill updatedBill, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        billRepository.findByDocNoAndUser(docNo, user).ifPresent(existingBill -> {
            existingBill.setDate(updatedBill.getDate());
            billRepository.save(existingBill);
        });
        return "redirect:/all-bills";
    }
    @GetMapping("/analytics")
    public String viewAnalytics(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        return "analytics";
    }


}