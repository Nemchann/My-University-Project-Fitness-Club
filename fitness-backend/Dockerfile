# Этап 1: Сборка (используем Maven и Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (кешируем этот слой)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск (используем только JRE для легкости)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный jar-файл из первого этапа
COPY --from=build /app/target/*.jar app.jar

# Открываем порт (тот самый, что будет в docker-compose)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]