# ====== Étape 1 : build de l’outil Spoon (SpoonLoggerBackend) ======
FROM maven:3.9.9-eclipse-temurin-21 AS spoon-build
WORKDIR /spoon

COPY API/SpoonLoggerBackend/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY API/SpoonLoggerBackend/src ./src
RUN mvn -q -DskipTests package
# -> /spoon/target/SpoonLoggerBackend-*.jar

# ====== Étape 2 : build du backend Spring Boot instrumenté ======
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le backend
COPY API/tp3-api/ /app/

# Copier l’outil Spoon
COPY --from=spoon-build /spoon/target/SpoonLoggerBackend-*.jar /tool.jar

# Config Spoon : on cible UNIQUEMENT les controllers
ENV IN_SRC=/app/src/main/java/com/example/tp3restructuring/controller
ENV OUT_DIR=/app/src-instrumented

# 1) Instrumenter UNIQUEMENT les controllers
RUN java -cp /tool.jar org.example.Main \
    # 2) Recopier les fichiers instrumentés à la place des controllers d'origine
 && mkdir -p /app/src/main/java/com/example/tp3restructuring/controller \
 && cp -r /app/src-instrumented/com/example/tp3restructuring/controller/* \
       /app/src/main/java/com/example/tp3restructuring/controller/

# 3) Builder le backend avec les controllers patchés
RUN mvn -q -e -DskipTests clean package

# ====== Étape 3 : image d’exécution ======
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN mkdir -p /var/log/app && chmod -R 777 /var/log/app

COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
