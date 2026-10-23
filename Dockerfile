# ── Etapa 1: compilación ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiamos el wrapper de Gradle primero para aprovechar la caché de capas
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Quita CRLF por si el wrapper se editó en Windows
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Descargamos dependencias antes de copiar el código fuente (optimización de caché)
RUN ./gradlew dependencies --no-daemon -q || true

# Copiamos el código fuente y generamos el JAR ejecutable
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ── Etapa 2: imagen de ejecución ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Creamos un usuario sin privilegios de root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiamos únicamente el JAR generado en la etapa anterior
COPY --from=builder /app/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
