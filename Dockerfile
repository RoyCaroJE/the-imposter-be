# =========================
# Build stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Inject PORT
ENV PORT=8080

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# RUN
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
