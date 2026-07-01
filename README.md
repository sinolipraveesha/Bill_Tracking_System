# Bill Tracking & Inventory Hub 📦✈️

A robust, cloud-native SaaS web application designed for a retail arbitrage and dropshipping business to digitalize paper store receipts, track dynamic inventory metrics, and streamline financial accounting.

🔗 **Live Demo:** [https://bill-tracking-system-obpe.onrender.com/](https://bill-tracking-system-obpe.onrender.com/)

---

## 🛠️ Tech Stack & Architecture

The system is built utilizing a modern, production-grade enterprise Java stack with a serverless data layer:

* **Core Framework:** Java 17, Spring Boot, Spring Web MVC
* **Security Layer:** Spring Security (Secure registration, multi-tenant session management)
* **Data & ORM:** Spring Data JPA, Hibernate, PostgreSQL
* **Database Cloud Platform:** Hosted Serverless via **Neon** (Auto-scaling, isolated connection pooling)
* **Frontend Engine:** Thymeleaf (HTML5 / CSS3 / Bootstrap) with full i18n Translation Support
* **DevOps & Deployment:** Containerized via Multi-stage **Docker**, Managed via Maven, Hosted on **Render**

---

## 💡 Core Engineering Highlights

### 1. Relational Database Architecture & Security Isolation
Architected a multi-tenant relational schema to guarantee complete user workspace isolation. Registered users interact exclusively with their own private data nodes.
* **User (`app_users`):** Handles secure authentication data, security recovery constraints, and localized language state configuration.
* **Bill (`bill`):** Holds store invoice metadata and maps explicitly via `ManyToOne` to the user.
* **Product (`product`):** Represents itemized inventory assets maps via `ManyToOne` back to parent bills.

### 2. Reactive Business Logic Engine
The backend code eliminates manual computation by automatically executing structural calculations directly within the entity layer:
* **Dynamic Cost Computations:** Automatically aggregates product price points and quantities to evaluate total bill values.
* **Automated Status Management:** Tracks global inventory distribution tags (`IN_STOCK`, `SOLD`, `MIXED`) reactively as product status values change.

### 3. Enterprise Integration Modules
* **Internationalization (i18n):** Deep translation integration allowing real-time language switching between **English (🇬🇧)** and **Italian (🇮🇹)** context maps.
* **Document Generation:** Bundled with OpenPDF compiling structured layout files into instantly downloadable invoice receipts for standard accounting workflows.

---

## 🐳 Docker Deployment Strategy

The application uses an optimized, multi-stage `Dockerfile` to build smaller, faster runtime footprints suited for cloud containers:

```dockerfile
# Stage 1: Compile and Build via Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Lightweight Runtime Environment
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
🚀 Local Installation & Setup
Prerequisites
Java Development Kit (JDK) 17 or higher

Apache Maven 3.8+

A running PostgreSQL instance (or cloud Neon connection string)

Steps
Clone the repository:

Bash
git clone [https://github.com/sinolipraveesha/Bill_Tracking_System.git](https://github.com/sinolipraveesha/Bill_Tracking_System.git)
cd Bill_Tracking_System
Configure Environment Variables:
Create environment variables or customize your src/main/resources/application.properties with your database credentials:

Properties
spring.datasource.url=jdbc:postgresql://your-neon-database-url:5432/dbname
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
Build and package the app:

Bash
mvn clean package -DskipTests
Run the executable JAR:

Bash
java -jar target/BillTrackingSystem-0.0.1-SNAPSHOT.jar
The application will boot locally at http://localhost:8080.
