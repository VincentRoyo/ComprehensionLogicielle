# TP3 – Logging & Observabilité Logicielle
**Master Génie Logiciel – Université de Montpellier**

Ce dépôt contient l’ensemble du projet réalisé dans le cadre du **TP3 – Introduction à la journalisation et à l’observabilité logicielle**.

L’objectif est de démontrer une **chaîne complète d’observabilité** incluant :
- une API Spring Boot instrumentée automatiquement avec **Spoon**,
- une journalisation structurée (JSON) via **SLF4J + Logback**,
- une agrégation automatique de logs en **profils utilisateurs**,
- un frontend **React** instrumenté avec **OpenTelemetry**,
- une visualisation des traces via **Jaeger**,
- une infrastructure **entièrement conteneurisée avec Docker Compose**.

---

## 📐 Architecture Générale

Le projet est composé de plusieurs briques indépendantes mais interconnectées :

- Backend **Spring Boot** (API REST sécurisée JWT)
- Instrumentation automatique du backend via **Spoon**
- Base de données **MongoDB**
- Frontend **React**
- Reverse proxy **Nginx**
- Observabilité **OpenTelemetry + Jaeger**
- Outil Java d’agrégation de logs (interface **Swing**)

L’ensemble est orchestré via **Docker Compose** afin de garantir reproductibilité et isolation.

---

## 📁 Structure du Dépôt

```
.
├── API/
│   ├── tp3-api/                 # API Spring Boot (code métier)
│   └── SpoonLoggerBackend/      # Instrumentation automatique des logs (Spoon)
│
├── Frontend/react/              # Application React instrumentée OpenTelemetry
│
├── ProfileAggregator/           # Outil Java d’agrégation de logs (Swing + JSON)
│
├── MongoDB/                     # Initialisation MongoDB (données de test)
├── logs/
│   ├── APIGenerated/            # Logs JSON générés par l’API
│   └── Aggregated/              # Profils utilisateurs calculés
│
├── docker-compose.yml           # Orchestration globale
├── nginx.conf                   # Reverse proxy (API / Front / OTEL)
├── otelcol.yaml                 # Configuration OpenTelemetry Collector
```

---

## 🚀 Lancement Rapide

### Prérequis
- Docker ≥ 24
- Docker Compose v2
- JDK 21 requis pour lancer l'agrégateur de profils

### Lancer l’ensemble de la stack
```bash
docker compose up --build
```

Cette commande :
- instrumente automatiquement l’API avec Spoon,
- initialise MongoDB avec des données de test,
- démarre l’API Spring Boot,
- démarre le frontend React,
- active OpenTelemetry et Jaeger,
- monte les volumes de logs.

---

## 🌐 Accès aux Services

| Service | URL |
|------|----|
| Frontend React | https://127.0.0.1/ |
| API REST | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui/index.html |
| Jaeger (traces) | http://localhost:16686 |
| Mongo Express | http://localhost:8081 |

---

## 🧠 Backend – API Spring Boot

### Emplacement
```
API/tp3-api/
```

### Responsabilités
- Gestion des utilisateurs et produits
- Sécurisation par JWT
- Exposition d’API REST
- Production de logs structurés JSON

### Points clés à modifier

| Besoin | Fichier |
|------|--------|
| Endpoints REST | controller/*Controller.java |
| Règles métier | Service/ProductService.java |
| Sécurité JWT | configuration/SecurityConfig.java |
| Configuration logs | resources/logback-spring.xml |

---

## 🔧 Instrumentation Automatique – Spoon

### Emplacement
```
API/SpoonLoggerBackend/
```

### Rôle
- Analyse du code source du backend
- Détection automatique des endpoints REST
- Injection automatique :
  - de l’annotation `@Slf4j`
  - d’un bloc de logging standardisé
  - du contexte utilisateur via MDC

### Classe centrale
```
processors/EndpointLoggerProcessor.java
```

Aucun log n’est écrit manuellement dans l’API.  
Toute modification de la politique de logging se fait dans ce module.

---

## 📊 Logs & Profils Utilisateurs

### Logs API
```
logs/APIGenerated/api.log
```
- Format JSON
- Un événement par appel API
- Contient utilisateur, type d’opération, ressource et filtres

### Agrégation des profils
```
ProfileAggregator/
```

Fonctionnalités :
- Lecture des logs JSON
- Calcul des statistiques par utilisateur
- Génération du fichier :
```
logs/Aggregated/profiles.json
```

L’outil peut être lancé via son interface Swing.

---

## 🖥️ Frontend React & OpenTelemetry

### Emplacement
```
Frontend/react/
```

### Fonctionnalités
- Interface utilisateur complète
- Consommation de l’API REST
- Instrumentation automatique des appels HTTP (fetch)
- Propagation des traces vers le backend

### Instrumentation OpenTelemetry
- Initialisation dans :
```
app/otel.ts
```
- Export OTLP via le reverse proxy :
```
/otel/v1/traces
```

---

## 🔍 Observabilité & Traces

### OpenTelemetry Collector
- Configuration : `otelcol.yaml`
- Réception OTLP
- Traitement batch
- Export vers Jaeger

### Jaeger
- Interface : http://localhost:16686
- Visualisation des traces corrélées frontend / backend

---

## 🧪 Debug & Reproductibilité

### Build détaillé (logs Spoon)
```bash
docker compose build --progress=plain
```

### Nettoyage complet
```bash
docker compose down -v
```

---

## 📌 Choix Techniques

- SLF4J + Logback (intégration Spring, simplicité)
- Instrumentation automatique (Spoon)
- Logs structurés JSON
- OpenTelemetry standard
- Docker pour éliminer les écarts d’environnement

---

## 👥 Auteurs

- Vincent Royo
- Loris Bord

Master Génie Logiciel – Université de Montpellier  
Année académique 2025–2026
