# Gym CRM System

A comprehensive Spring-based Customer Relationship Management (CRM) system for gym operations, managing trainers, trainees, and their training sessions.

## 🏗️ Architecture Overview

This application follows a layered architecture with Spring Boot framework, implementing:

- **Domain Models**: User (base), Trainer, Trainee, Training, TrainingType
- **Data Access Layer**: DAO interfaces and implementations with in-memory storage
- **Service Layer**: Business logic for trainer, trainee, and training management
- **Facade Layer**: Unified interface for all CRM operations
- **Configuration**: Spring Boot auto-configuration with annotation-based dependency injection

## 📋 Features

### Trainee Management
- ✅ Create trainee profile with auto-generated credentials
- ✅ Update trainee information
- ✅ Delete trainee profile
- ✅ Select/retrieve trainee details

### Trainer Management
- ✅ Create trainer profile with auto-generated credentials
- ✅ Update trainer information
- ✅ Select/retrieve trainer details

### Training Management
- ✅ Create training sessions
- ✅ Select training details
- ✅ View trainings by trainee
- ✅ View trainings by trainer

### Automatic Credential Generation
- 📧 **Username**: `FirstName.LastName` format with serial numbers for duplicates
- 🔒 **Password**: Random 10-character alphanumeric string

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Java Version**: 21
- **Build Tool**: Maven
- **Testing**: JUnit 5 + Mockito
- **Data Storage**: In-memory Maps with file initialization
- **Dependency Injection**: Mixed approach (constructor, setter, autowiring)
- **Logging**: SLF4J with Logback

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/gym/crm/
│   │   ├── dao/              # Data access interfaces & implementations
│   │   ├── facade/           # Facade pattern implementation
│   │   ├── model/            # Domain entities
│   │   ├── service/          # Business logic layer
│   │   ├── storage/          # In-memory storage implementations
│   │   └── util/             # Utility classes (credential generation)
│   └── resources/
│       ├── data/             # Initial data files
│       └── application.properties
└── test/
    └── java/com/gym/crm/     # Comprehensive unit & integration tests
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd crm
   ```

2. **Run tests**
   ```bash
   mvn test
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Initial Data

The system automatically loads initial data from files in `src/main/resources/data/`:
- `trainers.txt` - Initial trainer data
- `trainees.txt` - Initial trainee data  
- `trainings.txt` - Initial training sessions
- `training-types.txt` - Available training types

### Configuration

Modify `application.properties` to customize:
- Logging levels
- Data file paths
- Application settings

## 🧪 Testing

The project includes comprehensive test coverage:

- **Unit Tests**: All service classes, DAOs, and utilities
- **Integration Tests**: Full application context and workflow testing
- **Mocking**: Mockito for isolated unit testing

Run tests:
```bash
mvn test
```

## 💡 Usage Examples

### Creating a Trainee
```java
@Autowired
private GymCrmFacade facade;

Trainee trainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
Trainee created = facade.createTrainee(trainee);
// Username: John.Doe, Password: auto-generated
```

### Creating a Training Session
```java
Training training = new Training(traineeId, trainerId, "Morning Workout", 
                               trainingTypeId, LocalDate.now(), 60);
Training created = facade.createTraining(training);
```

### Retrieving Data
```java
// Get all trainees
List<Trainee> trainees = facade.getAllTrainees();

// Get trainings for a specific trainee
List<Training> trainings = facade.getTrainingsByTrainee(traineeId);
```

## 🔧 Dependency Injection Strategy

As per requirements, the system uses different injection strategies:

- **Storage → DAO**: Autowiring with `@Autowired` on fields
- **DAO → Services**: Setter-based injection with `@Autowired` on setter methods  
- **Services → Facade**: Constructor-based injection

## 📊 Logging

The application provides comprehensive logging:
- **DEBUG**: Detailed operations and data access
- **INFO**: Service operations and application lifecycle
- **ERROR**: Exception handling and critical issues

## 🏛️ Design Patterns

- **Facade Pattern**: Unified interface for all operations
- **DAO Pattern**: Data access abstraction
- **Storage Pattern**: Separate storage beans per entity type
- **Factory Pattern**: Credential generation utilities

## 📈 Performance Features

- **In-Memory Storage**: Fast data access
- **Concurrent Safe**: AtomicLong for ID generation
- **Lazy Loading**: On-demand data initialization
- **Efficient Filtering**: Stream API for data queries

## 🔒 Security Features

- **Secure Password Generation**: Cryptographically secure random
- **Input Validation**: Comprehensive validation throughout layers
- **Error Handling**: Graceful exception management

## 🚀 Future Enhancements

- Database integration (JPA/Hibernate)
- REST API endpoints
- Authentication & authorization
- Caching mechanisms
- Monitoring & metrics

## 📄 License

This project is part of a Spring Core learning exercise.

---

**Built with ❤️ using Spring Framework** 