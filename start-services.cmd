@echo off

echo Starting Eureka Server...
start "Eureka Server" cmd /k java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
timeout /t 10

echo Starting Config Server...
start "Config Server" cmd /k java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
timeout /t 10

echo Starting Booking Service...
start "Booking Service" cmd /k java -jar booking-service/target/booking-service-0.0.1-SNAPSHOT.jar

echo Starting Flight Service...
start "Flight Service" cmd /k java -jar flight-service/target/flight-service-0.0.1-SNAPSHOT.jar

echo Starting Auth Service...
start "Auth Service" cmd /k java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar

echo Starting API Gateway...
start "API Gateway" cmd /k java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar

echo All services started.
pause
