package com.example.billtrackingsystem.repository;

import com.example.billtrackingsystem.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.billtrackingsystem.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, String> {
    List<Bill> findByUser(User user);
    List<Bill> findByUserIsNull();
    Optional<Bill> findByDocNoAndUser(String docNo, User user);
    List<Bill> findByDocNoContainingAndUser(String docNo, User user);
}