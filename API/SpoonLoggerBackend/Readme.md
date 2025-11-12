# 🧠 SpoonLoggerBackend
### Instrumentation automatique du backend Spring Boot avec Spoon + Lombok + SLF4J

---

## 📌 Objectif
Ce projet utilise **[Spoon](https://spoon.gforge.inria.fr/)** pour **analyser et modifier automatiquement le code source Java** d’un backend Spring Boot avant sa compilation.  
L’objectif est d’injecter du **logging structuré** (via Lombok `@Slf4j` et SLF4J) dans les contrôleurs REST afin d’observer et profiler les comportements utilisateurs.

---

## ⚙️ Fonctionnement général

### 🔹 Étapes automatiques (via Docker)
1. **Le projet SpoonLoggerBackend** est compilé dans le stage `spoon-build` du Dockerfile.
2. **L’outil Spoon** parcourt les sources Java du backend (`tp3-api/src/main/java`).
3. Il **détecte toutes les méthodes exposées par Spring Boot** (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, etc.).
4. Il y **injecte automatiquement** en début de méthode :
   ```java
   // __instrumented_api_call__
   MDC.put("opType", "READ" | "WRITE" | "SEARCH_EXPENSIVE");
   MDC.put("resource", "products" | "users" | ...);
   MDC.put("path", "/api/...");
   log.info("api_call begin");
   ```
5. Si la classe ne possède pas déjà un logger, l’annotation **`@Slf4j`** est ajoutée.
6. Le code instrumenté est ensuite recompilé en JAR Spring Boot.

---

## 📂 Structure du dépôt

```
API/
├── tp3-api/              ← Backend Spring Boot
│   ├── src/main/java/
│   └── pom.xml
├── SpoonLoggerBackend/   ← Projet Spoon (instrumentation)
│   ├── src/main/java/
│   └── pom.xml
└── Dockerfile            ← Pipeline Docker multi-stage
```

---

## 🧾 Exemple de log produit (JSON Logback)

```json
{
  "timestamp": "2025-11-12T14:33:00.312Z",
  "level": "INFO",
  "logger": "com.example.api.ProductController",
  "message": "api_call begin",
  "mdc": {
    "userId": "alice",
    "opType": "READ",
    "resource": "products",
    "path": "/api/products"
  }
}
```

---

## 🧠 Objectif pédagogique

L’instrumentation permet ensuite de **construire des profils utilisateurs** à partir des logs :
- 🔹 *Mostly Readers* → effectuent principalement des opérations `GET`
- 🔹 *Mostly Writers* → effectuent surtout des `POST/PUT/DELETE`
- 🔹 *Hunters* → recherchent souvent les produits les plus chers

Ces profils peuvent être générés depuis les logs JSON via un job d’agrégation (Java, Python, etc.).

---

## 🛠️ Technologies principales
- **Java 21**
- **Spoon 10.4.2** (analyse et transformation AST)
- **SLF4J + Lombok `@Slf4j`**
- **Spring Boot 3.5+**
- **Docker multi-stage build**

---

## 🧩 Auteur & Licence
Projet conçu à des fins pédagogiques dans le cadre d’un TP d’analyse de code.  
Licence : MIT