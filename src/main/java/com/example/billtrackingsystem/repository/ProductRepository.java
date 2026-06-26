package com.example.billtrackingsystem.repository;

import com.example.billtrackingsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import com.example.billtrackingsystem.model.User;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // This finds the most recent entry for this IAN/AIN so we can copy the name and price, filtered by user
    @Query("SELECT p FROM Product p WHERE p.ian = :ian AND p.user = :user ORDER BY p.id DESC LIMIT 1")
    Optional<Product> findTopByIanAndUser(@Param("ian") String ian, @Param("user") User user);

    // Your existing search method, filtered by user
    @Query("SELECT p FROM Product p WHERE p.ian = :ian AND p.user = :user")
    List<Product> findProductDetailsByIanAndUser(@Param("ian") String ian, @Param("user") User user);

    List<Product> findByUserIsNull();
}