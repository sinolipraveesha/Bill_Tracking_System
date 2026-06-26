package com.example.billtrackingsystem.config;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.billtrackingsystem.model.Bill;
import com.example.billtrackingsystem.repository.BillRepository;
import com.example.billtrackingsystem.repository.ProductRepository;
import java.util.List;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) throws Exception {
        // Ensure user_id column and foreign keys exist in the lowercase 'bill' and 'product' tables
        try {
            System.out.println("Running database schema migrations...");
            
            // 1. Alter bill table
            jdbcTemplate.execute("ALTER TABLE bill ADD COLUMN IF NOT EXISTS user_id bigint");
            
            // 2. Alter product table
            jdbcTemplate.execute("ALTER TABLE product ADD COLUMN IF NOT EXISTS user_id bigint");
            
            // 3. Add foreign key to bill table if not exists
            jdbcTemplate.execute("DO $$\n" +
                    "BEGIN\n" +
                    "    IF NOT EXISTS (\n" +
                    "        SELECT 1 FROM information_schema.table_constraints \n" +
                    "        WHERE constraint_name = 'fk_bill_user' AND table_name = 'bill'\n" +
                    "    ) THEN\n" +
                    "        ALTER TABLE bill ADD CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES app_users(id);\n" +
                    "    END IF;\n" +
                    "END $$;");
            
            // 4. Add foreign key to product table if not exists
            jdbcTemplate.execute("DO $$\n" +
                    "BEGIN\n" +
                    "    IF NOT EXISTS (\n" +
                    "        SELECT 1 FROM information_schema.table_constraints \n" +
                    "        WHERE constraint_name = 'fk_product_user' AND table_name = 'product'\n" +
                    "    ) THEN\n" +
                    "        ALTER TABLE product ADD CONSTRAINT fk_product_user FOREIGN KEY (user_id) REFERENCES app_users(id);\n" +
                    "    END IF;\n" +
                    "END $$;");
            
            System.out.println("Database schema migrations completed successfully.");
        } catch (Exception e) {
            System.err.println("Database schema migration failed: " + e.getMessage());
            e.printStackTrace();
        }
        // Find or initialize the admin user.
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword("password123");
            admin.setRecoveryQuestion("What is your default recovery key?");
            admin.setRecoveryAnswer("admin123");
            admin.setPreferredLanguage("en");
            admin = userRepository.save(admin);
            System.out.println("--------------------------------------------------");
            System.out.println("Default Admin User Initialized successfully!");
            System.out.println("Username: admin");
            System.out.println("Password: password123");
            System.out.println("Recovery Question: What is your default recovery key?");
            System.out.println("Recovery Answer: admin123");
            System.out.println("Preferred Language: en");
            System.out.println("Please change these default credentials immediately.");
            System.out.println("--------------------------------------------------");
        }

        // Migrate existing orphan bills/products to the admin user using bulk updates
        System.out.println("Migrating orphan bills to admin user...");
        int updatedBills = jdbcTemplate.update("UPDATE bill SET user_id = ? WHERE user_id IS NULL", admin.getId());
        System.out.println("Migrated " + updatedBills + " orphan bills.");

        System.out.println("Migrating orphan products to admin user...");
        int updatedProducts = jdbcTemplate.update("UPDATE product SET user_id = ? WHERE user_id IS NULL", admin.getId());
        System.out.println("Migrated " + updatedProducts + " orphan products.");
    }
}
