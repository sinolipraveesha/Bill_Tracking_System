package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.Bill;
import com.example.billtrackingsystem.model.ProductSearchResult;
import com.example.billtrackingsystem.repository.BillRepository;
import com.example.billtrackingsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.billtrackingsystem.model.Product;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class BillController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductRepository productRepository;

    // 1. MAIN LIST & SEARCH BY DOC NO
    @GetMapping("/all-bills")
    public String viewAllBills(@RequestParam(required = false) String docNo, Model model) {
        List<Bill> bills;
        if (docNo != null && !docNo.isEmpty()) {
            // Search by Doc No (Bill ID)
            bills = billRepository.findById(docNo).map(List::of).orElse(List.of());
        } else {
            // Show everything if no search term
            bills = billRepository.findAll();
        }
        model.addAttribute("bills", bills);
        return "view-bills";
    }

    // 2. VIEW SPECIFIC BILL DETAILS
    @GetMapping("/view-bill/{docNo}")
    public String viewBillDetails(@PathVariable String docNo, Model model) {
        Optional<Bill> bill = billRepository.findById(docNo);
        if (bill.isPresent()) {
            model.addAttribute("bill", bill.get());
            return "bill-details";
        }
        return "redirect:/all-bills";
    }

    // 3. DELETE A BILL
    @PostMapping("/delete-bill/{docNo}")
    public String deleteBill(@PathVariable String docNo) {
        billRepository.deleteById(docNo);
        return "redirect:/all-bills";
    }

    // 4. SEARCH BY IAN NUMBER
    @GetMapping("/search-ian")
    public String searchByIan(@RequestParam(required = false) String ian, Model model) {
        if (ian != null && !ian.isEmpty()) {
            // FIX: We now fetch a List of Product instead of ProductSearchResult
            List<Product> products = productRepository.findProductDetailsByIan(ian);
            model.addAttribute("results", products);
            model.addAttribute("searchedIan", ian);
        }
        return "ian-search-results";
    }

    @GetMapping("/export-pdf/{docNo}")
    public void exportToPDF(@PathVariable String docNo, HttpServletResponse response) throws IOException {
        Optional<Bill> optBill = billRepository.findById(docNo);
        if (optBill.isEmpty()) return;
        Bill bill = optBill.get();

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bill_" + docNo + ".pdf";
        response.setHeader(headerKey, headerValue);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);

        Paragraph title = new Paragraph("INVOICE - " + bill.getDocNo(), fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Date: " + bill.getDate()));
        document.add(new Paragraph(" ")); // Spacer

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell("IAN Number");
        table.addCell("Product Name");
        table.addCell("Price (LKR)");

        for (Product product : bill.getProducts()) {
            table.addCell(product.getIan());
            table.addCell(product.getName());
            table.addCell(String.valueOf(product.getPrice()));
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Amount: LKR " + bill.getTotalAmount()));

        document.close();
    }


    // --- NEW: SHOW UPDATE FORM ---
    @GetMapping("/edit-bill/{docNo}")
    public String showUpdateForm(@PathVariable String docNo, Model model) {
        Optional<Bill> bill = billRepository.findById(docNo);
        if (bill.isPresent()) {
            model.addAttribute("bill", bill.get());
            return "update-bill";
        }
        return "redirect:/all-bills";
    }

    // --- NEW: PROCESS UPDATE ---
    @PostMapping("/update-bill/{docNo}")
    public String updateBill(@PathVariable String docNo, @ModelAttribute("bill") Bill updatedBill) {
        billRepository.findById(docNo).ifPresent(existingBill -> {
            existingBill.setDate(updatedBill.getDate());
            // Note: In simple HTML, we usually update the header.
            // If you want to update products, we usually do that via the API or a more complex form.
            billRepository.save(existingBill);
        });
        return "redirect:/all-bills";
    }


}