package com.example.billtrackingsystem.repository;

import com.example.billtrackingsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // This finds the most recent entry for this IAN/AIN so we can copy the name and price
    @Query("SELECT p FROM Product p WHERE p.ian = :ian ORDER BY p.id DESC LIMIT 1")
    Optional<Product> findTopByIanOrderByIdDesc(@Param("ian") String ian);

    // Your existing search method
    @Query("SELECT p FROM Product p WHERE p.ian = :ian")
    java.util.List<Product> findProductDetailsByIan(@Param("ian") String ian);
}