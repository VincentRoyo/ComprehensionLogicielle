# ====== Étape 1 : build de l’outil Spoon (SpoonLoggerBackend) ======
FROM maven:3.9.9-eclipse-temurin-21 AS spoon-build
WORKDIR /spoon

# Copier le projet Spoon (depuis la racine du context)
COPY API/SpoonLoggerBackend/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY API/SpoonLoggerBackend/src ./src
RUN mvn -q -DskipTests package
# jar → /spoon/target/SpoonLoggerBackend-*.jar

# ====== Étape 2 : build du backend Spring Boot instrumenté ======
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le projet backend (depuis la racine du context)
COPY API/tp3-api/ /app/

# Copier l’outil Spoon depuis l’étape précédente
COPY --from=spoon-build /spoon/target/SpoonLoggerBackend-*.jar /tool.jar

# Chemins attendus par Main.java
ENV IN_SRC=/app/src/main/java
ENV OUT_DIR=/app/src-instrumented

# 1) Instrumenter  2) Remplacer sources  3) Builder
RUN java -cp /tool.jar org.example.Main \
 && rm -rf /app/src/main/java \
 && mkdir -p /app/src/main \
 && mv /app/src-instrumented /app/src/main/java \
 && mvn -q -e -DskipTests clean package

# ====== Étape 3 : image d’exécution ======
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

