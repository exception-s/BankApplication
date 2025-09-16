# Local Bank App

Микросервисное банковское приложение на Java с использованием Spring Boot, обеспечивающее основные банковские операции через REST API.

## Архитектура

Приложение построено по многослойной архитектуре:

```
┌─────────────────────────────────────────────────┐
│                  Presentation Layer              │
│               (REST Controllers)                 │
└─────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────┐
│                 Service Layer                    │
│          (Business Logic Implementation)         │
└─────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────┐
│                 Repository Layer                 │
│           (Data Access & Persistence)            │
└─────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────┐
│                 Database Layer                   │
│              (PostgreSQL Database)               │
└─────────────────────────────────────────────────┘
```

### Основные компоненты:
- **Контроллеры**: Обработка HTTP-запросов и валидация
- **Сервисы**: Бизнес-логика и транзакции
- **Репозитории**: Доступ к данным через Spring Data JPA
- **Сущности**: Модели данных (Пользователи, Счета, Транзакции)
- **Конфигурация**: Настройки безопасности и приложения

## Технологии

- **Backend**: Java 21, Spring Boot 3.5.0
- **База данных**: PostgreSQL 13
- **Безопасность**: Spring Security, JWT-аутентификация
- **Документация**: Springdoc OpenAPI (Swagger UI)
- **Контейнеризация**: Docker, Docker Compose
- **Тестирование**: JUnit 5, Mockito, Testcontainers
- **Сборка**: Maven

## Установка и запуск

### Предварительные требования:
- Java 21 или выше
- Maven 3.8+
- Docker и Docker Compose

### Запуск через Docker Compose:
```bash
# Клонирование репозитория
git clone https://github.com/exception-s/BankApplication.git
cd BankApplication

# Запуск приложения с базой данных
docker-compose up --build
```

Приложение будет доступно по адресу: http://localhost:8080

## Аутентификация

Приложение использует JWT-аутентификацию. Для доступа к защищенным endpoint'ам необходимо:
1. Зарегистрировать пользователя
2. Получить JWT-токен через endpoint аутентификации
3. Добавлять токен в заголовок запроса: `Authorization: Bearer <token>`

## Примеры запросов

### Регистрация пользователя
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123",
    "email": "john.doe@example.com"
  }'
```

### Аутентификация
```bash
curl -X POST "http://localhost:8080/api/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

Ответ:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Создание банковского счета
```bash
curl -X POST "http://localhost:8080/api/accounts" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "currency": "USD"
  }'
```

### Проверка баланса
```bash
curl -X GET "http://localhost:8080/api/accounts/1/balance" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Перевод средств
```bash
curl -X POST "http://localhost:8080/api/transactions/transfer" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 150.50,
    "description": "Payment for services",
    "fromCurrency": "USD",
    "toCurrency": "USD"
  }'
```

### Получение истории транзакций
```bash
curl -X GET "http://localhost:8080/api/transactions/account/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Тестирование

### Запуск тестов
```bash
# Все тесты
mvn test

# Только модульные тесты
mvn test -Dtest=*UnitTest

# Только интеграционные тесты
mvn test -Dtest=*IntegrationTest
```

### Структура тестов:
- **Модульные тесты**: Тестирование отдельных компонентов (сервисы, утилиты)
- **Тесты контроллеров**: Тестирование API endpoints
- **Интеграционные тесты**: Полные сценарии с Testcontainers


## База данных

### Схема базы данных:
- **users**: Информация о пользователях
- **user_roles**: Роли пользователей (USER, ADMIN)
- **bank_accounts**: Банковские счета
- **transactions**: История транзакций
