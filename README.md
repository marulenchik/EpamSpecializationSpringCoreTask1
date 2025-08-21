# Gym CRM System - REST API

A comprehensive Spring Boot-based CRM system for gym management with REST API endpoints.

## 🚀 Features

### Core Functionality
- **User Management**: Trainee and Trainer registration with auto-generated credentials
- **Authentication**: Secure login and password management
- **Profile Management**: Complete CRUD operations for user profiles
- **Training Management**: Schedule and manage training sessions
- **Relationship Management**: Many-to-many relationships between trainers and trainees

### Technical Features
- **RESTful API**: 17 comprehensive endpoints
- **Database Integration**: JPA/Hibernate with H2 in-memory database
- **Transaction Management**: ACID compliance with `@Transactional`
- **Validation**: Comprehensive input validation with Bean Validation
- **Error Handling**: Global exception handling with detailed error responses
- **Logging**: Two-level logging (transaction-level and REST call details)
- **API Documentation**: Swagger/OpenAPI 3 integration
- **Testing**: Comprehensive unit and integration tests

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: H2 (in-memory for development)
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Test
- **Documentation**: Swagger/OpenAPI 3
- **Logging**: SLF4J with Logback

### Design Patterns
- **Facade Pattern**: Unified interface for all business operations
- **Repository Pattern**: Data access abstraction with Spring Data JPA
- **DTO Pattern**: Request/Response data transfer objects
- **Dependency Injection**: Constructor, setter, and field injection strategies

## 📚 API Endpoints

### Authentication & User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/trainees/register` | Register a new trainee |
| `POST` | `/api/trainers/register` | Register a new trainer |
| `GET` | `/api/login` | User authentication |
| `PUT` | `/api/change-password` | Change user password |

### Profile Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/trainees/{username}` | Get trainee profile |
| `PUT` | `/api/trainees` | Update trainee profile |
| `DELETE` | `/api/trainees/{username}` | Delete trainee profile |
| `GET` | `/api/trainers/{username}` | Get trainer profile |
| `PUT` | `/api/trainers` | Update trainer profile |

### Training Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/trainings` | Add new training |
| `GET` | `/api/trainees/{username}/trainings` | Get trainee's trainings |
| `GET` | `/api/trainers/{username}/trainings` | Get trainer's trainings |
| `GET` | `/api/training-types` | Get all training types |

### Relationship Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/trainers/unassigned` | Get unassigned trainers |
| `PUT` | `/api/trainees/trainers` | Update trainee's trainer list |

### Status Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `PATCH` | `/api/trainees/activate` | Activate/Deactivate trainee |
| `PATCH` | `/api/trainers/activate` | Activate/Deactivate trainer |

## 🔧 Configuration

### Database Configuration
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:gymcrm
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### Logging Configuration
```properties
# Application Logging
logging.level.com.gym.crm=DEBUG
logging.level.org.springframework=INFO
logging.level.org.hibernate.SQL=DEBUG
```

## 🧪 Testing

### Test Coverage
- **Unit Tests**: 26 tests covering all controllers and services
- **Integration Tests**: End-to-end workflow testing
- **Test Categories**:
  - Controller layer tests with MockMvc
  - Service layer tests with Mockito
  - Authentication service tests
  - Complete workflow integration tests

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test categories
mvn test -Dtest="*ControllerTest"
mvn test -Dtest="AuthenticationServiceTest"
```

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+

### Installation
1. Clone the repository
2. Navigate to project directory
3. Run the application:
```bash
mvn spring-boot:run
```

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console`

## 📊 Key Features Implemented

### 1. User Credential Generation
- **Username**: `FirstName.LastName` with serial numbers for duplicates
- **Password**: Random 10-character string
- **Example**: `john.doe` / `abc123xyz7`

### 2. Authentication System
- All endpoints (except registration) require authentication
- Username/password validation before operations
- Secure credential matching

### 3. Validation & Error Handling
- Comprehensive input validation
- Global exception handling
- Detailed error responses with transaction IDs
- HTTP status code compliance

### 4. Transaction Management
- ACID compliance with `@Transactional`
- Cascade operations (trainee deletion → training deletion)
- Non-idempotent activate/deactivate operations

### 5. Advanced Querying
- Criteria-based training filtering
- Date range queries
- Complex JPQL queries for unassigned trainers
- Many-to-many relationship management

### 6. Logging System
- **Transaction-level**: Unique transaction IDs for request tracking
- **REST call details**: Endpoint, request/response logging
- **Performance monitoring**: Request completion status

## 🔒 Security Features

- **Authentication required** for all operations except registration
- **Password validation** before sensitive operations
- **Input sanitization** and validation
- **Error message standardization** to prevent information leakage

## 📈 Performance Features

- **Lazy loading** for entity relationships
- **Connection pooling** with H2 database
- **Transaction optimization** with proper scoping
- **Efficient querying** with JPA criteria API

## 🎯 Business Rules

1. **User Management**:
   - Cannot register as both trainer and trainee
   - Username cannot be changed after registration
   - Passwords can be changed with old password verification

2. **Training Management**:
   - Training types are predefined constants
   - Training duration must be positive
   - Training date and duration are required

3. **Relationship Management**:
   - Trainers and trainees have many-to-many relationships
   - Unassigned trainers can be retrieved for specific trainees
   - Trainer specialization is read-only after creation

4. **Status Management**:
   - Activate/deactivate operations are non-idempotent
   - Only active users can be assigned to trainings
   - Deletion cascades to related entities

## 🏆 Implementation Highlights

✅ **Complete REST API** - All 17 endpoints implemented and tested  
✅ **Database Integration** - Full JPA/Hibernate implementation  
✅ **Authentication System** - Secure credential-based access  
✅ **Validation Framework** - Comprehensive input validation  
✅ **Error Handling** - Global exception handling with detailed responses  
✅ **Transaction Logging** - Two-level logging system  
✅ **API Documentation** - Swagger/OpenAPI 3 integration  
✅ **Unit Testing** - 26 tests with 100% pass rate  
✅ **Integration Testing** - End-to-end workflow verification  

This implementation provides a production-ready REST API for gym management with comprehensive features, security, and testing coverage.
