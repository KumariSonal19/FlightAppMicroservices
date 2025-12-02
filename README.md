# Flight Booking Microservices System

A robust, event-driven microservices application for searching and booking flights. This system is built using **Spring Boot** and **Spring Cloud**, featuring service discovery, centralized configuration, API Gateway routing, circuit breakers, and asynchronous email notifications via RabbitMQ.

## Architecture

The system consists of the following microservices:

| Service | Port | Description |
| :--- | :--- | :--- |
| **Config Server** | `8888` | Centralized configuration for all services. |
| **Eureka Server** | `8761` | Service Registry & Discovery. |
| **API Gateway** | `8081` | Single entry point, handles routing and load balancing. |
| **Flight Service** | `8083` | Manages flight inventory, searching, and seat availability (MongoDB). |
| **Booking Service** | `8082` | Handles ticket booking, passenger info, and email notifications (MongoDB + RabbitMQ). |

## Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.2.x, Spring Cloud 2023.x
* **Database:** MongoDB
* **Messaging:** RabbitMQ
* **Resilience:** Resilience4j (Circuit Breaker)
* **Communication:** OpenFeign (Sync)
* **Testing:** JUnit 5, Mockito, JaCoCo, JMeter
* **Build Tool:** Maven

## Prerequisites

Before running the application, ensure you have the following installed and running:

1.  **Java 17+**
2.  **Maven 3.8+**
3.  **MongoDB** (Running on `localhost:27017`)
4.  **RabbitMQ** (Running on `localhost:5672`)

## API ENDPOINTS

- **Add Flight Inventory**  
  `POST /api/flight/airline/inventory`  
  Validates and adds flight data, ensuring no duplicates or conflicts. Responds with `201 Created` on success.

- **Search Flights**  
  `POST /api/flight/search`  
  Supports one-way and round-trip search with date-range filtering. Returns matching flights or `404 Not Found`.

- **Book Ticket**  
  `POST /api/flight/booking/{flightId}`  
  Validates passenger info, checks seat availability, deducts seats, creates booking with auto-generated PNR, returns `201 Created`.

- **Cancel Ticket**  
  `DELETE /api/flight/booking/cancel/{pnr}`  
  Applies 24-hour cancellation rule, marks booking as cancelled, restores available seats, returns `200 OK`.

- **Get Ticket by PNR**  
  `GET /api/flight/ticket/{pnr}`  
  Retrieves booking and passenger details, returns `200 OK` or `404 Not Found`.

- **Booking History by Email**  
  `GET /api/flight/booking/history/{email}`  
  Streams all past bookings for a user via Flux response.
