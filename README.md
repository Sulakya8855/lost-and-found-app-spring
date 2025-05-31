# Lost and Found App - Spring Boot

A comprehensive Lost and Found management system built with Spring Boot, featuring secure user authentication, item management, and claim processing.

## 🚀 Features

### Core Functionality
- **Item Management**: Report lost or found items with detailed descriptions
- **User Authentication**: Secure JWT-based authentication system
- **Claim Processing**: Request and approve/reject claims for found items
- **Role-Based Access**: Admin, Staff, and User roles with different permissions
- **Real-time Status Tracking**: Track items through Lost → Found → Claimed workflow

### Security Features
- JWT token-based authentication
- BCrypt password encryption
- Role-based authorization (@PreAuthorize)
- CORS configuration for cross-origin requests
- Security filter chain with method-level security

### API Features
- RESTful API endpoints
- Swagger/OpenAPI documentation (`/swagger-ui.html`)
- Comprehensive error handling and logging
- Transaction management for data consistency

## 📋 Requirements

### System Requirements
- **Java**: 17 or higher
- **Maven**: 3.6+ (or use included Maven wrapper)
- **Database**: MySQL 8.0+
- **IDE**: IntelliJ IDEA (recommended)


## 🛠️ Setup Instructions

### 1. Prerequisites
1. **Install Java 17+**
   ```bash
   java -version
   ```

2. **Install MySQL**
   - Download and install MySQL 8.0+
   - Create a database named `lost_and_found_db`

3. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd lost-and-found-app-spring
   ```

### 2. Database Configuration
1. **Create MySQL Database**
   ```sql
   CREATE DATABASE lost_and_found_db;
   CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'mypassword';
   GRANT ALL PRIVILEGES ON lost_and_found_db.* TO 'myuser'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Update Application Properties** (if needed)
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/lost_and_found_db
   spring.datasource.username=myuser
   spring.datasource.password=mypassword
   ```

### 3. IntelliJ IDEA Setup

#### Option A: Import Existing Project
1. **Open IntelliJ IDEA**
2. **File → Open** and select the project folder
3. **Import as Maven Project** when prompted
4. Wait for Maven to download dependencies

#### Option B: Clone in IntelliJ
1. **File → New → Project from Version Control**
2. Enter repository URL
3. Choose project directory
4. IntelliJ will automatically detect Maven configuration

#### Project Configuration in IntelliJ
1. **Set Project SDK**
   - **File → Project Structure → Project**
   - Set **Project SDK** to Java 17+
   - Set **Language Level** to 17

2. **Configure Maven**
   - **File → Settings → Build, Execution, Deployment → Build Tools → Maven**
   - Ensure **Maven home path** is correctly set
   - Check **Import Maven projects automatically**

3. **Enable Annotation Processing** (for Lombok)
   - **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors**
   - Check **Enable annotation processing**

4. **Install Lombok Plugin**
   - **File → Settings → Plugins**
   - Search for "Lombok" and install if not already present

### 4. Running the Application

#### Using IntelliJ IDEA
1. **Navigate to Main Class**
   - `src/main/java/com/crs/lost_and_found_app/LostAndFoundAppApplication.java`
2. **Right-click → Run 'LostAndFoundAppApplication'**




### Creating an Admin User

An administrator account is essential for managing the application.
If an admin user doesn't already exist, you can create one using the general registration endpoint detailed below.
To do so, send a POST request to `/api/v1/auth/signup` with the `role` field in the JSON payload set to `"ADMIN"`.


## 🗄️ Database Schema

### Users Table
- ID, username, email, password, role
- Created/updated timestamps

### Items Table
- ID, name, description, category, location
- Status, reported_by, held_by, claimed_by
- Date reported, timestamps

### Requests Table
- ID, item_id, requester_id, status
- Message, admin_notes, request_date, resolution_date
- Timestamps

## 🔧 Configuration

### JWT Configuration
- Secret key (change in production!)
- Token expiration: 1 hour (configurable)
- Claims include: role, userId, email

### Security Configuration
- CORS enabled for cross-origin requests
- CSRF disabled for stateless API
- Session management: Stateless

## 🐳 Docker Support

Build and run with Docker:
```bash
# Build image
docker build -t lost-and-found-app .

# Run container
docker run -p 8080:8080 lost-and-found-app
```

## 📊 Development Tools

### Swagger Documentation
Access API documentation at: `http://localhost:8080/swagger-ui.html`

### Postman Collection
Import `postman_api.json` for pre-configured API requests
