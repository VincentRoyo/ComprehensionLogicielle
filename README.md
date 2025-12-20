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

Le backend constitue le cœur fonctionnel de l’application. Il expose une API REST permettant aux utilisateurs d’interagir avec les ressources métier (produits, utilisateurs, actions), tout en servant de point d’ancrage principal pour la collecte des logs.  
Ce choix architectural reflète un cas d’usage réaliste d’application web, où les interactions client sont centralisées via des appels HTTP, facilitant ainsi l’observabilité, l’instrumentation et l’analyse des comportements utilisateurs.

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

Afin d’assurer une collecte homogène et systématique des logs applicatifs, l’instrumentation du backend est réalisée automatiquement à l’aide de Spoon.  
Plutôt que d’ajouter manuellement des appels de journalisation dans le code source, Spoon permet d’analyser et de transformer le code Java à la compilation, en injectant des points de log aux endroits stratégiques (entrée/sortie de méthodes, contrôleurs REST, etc.). Cette approche garantit la reproductibilité de l’instrumentation, limite les erreurs humaines et facilite l’expérimentation autour de l’observabilité logicielle.

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

Le module **ProfileAggregator** fournit une petite interface **Swing** permettant de piloter l’agrégation sans passer par la ligne de commande.

L’interface sert principalement à :
- choisir le fichier de logs JSON en entrée (par défaut : `logs/APIGenerated/api.log`) ;
- lancer l’agrégation et générer/écraser le fichier de sortie (par défaut : `logs/Aggregated/profiles.json`) ;
- visualiser un état d’avancement (console/zone de logs) et les erreurs de parsing éventuelles.

> L’objectif est de rendre l’outil utilisable “en démo” : on génère des logs via l’application, puis on reconstruit les profils utilisateurs en un clic.


### Exécution automatique de scénarios (génération de traces & profils)

En plus de l’agrégation, **ProfileAggregator** peut exécuter automatiquement une série de **scénarios utilisateurs** afin de produire des traces et des logs variés (connexion, lecture, actions sur les produits, etc.).
Ces scénarios ont pour but de simuler une utilisation “réaliste” de l’application (plusieurs utilisateurs, plusieurs types d’opérations), puis d’enchaîner directement avec l’agrégation des profils.

Concrètement, le mode “scénarios” :
1. déclenche une suite d’actions côté application (appels HTTP vers l’API) ;
2. génère ainsi des logs structurés côté backend (dans `logs/APIGenerated/`);
3. lance l’agrégation pour produire `logs/Aggregated/profiles.json`.

Cela permet d’obtenir en une exécution :
- des **logs** exploitables côté API ;
- des **profils agrégés** directement réutilisables pour l’analyse demandée dans le TP.

> Remarque : si l’exécution automatique est utilisée, assurez-vous que la stack Docker (API / proxy / front / observabilité) est déjà démarrée.


---

## 🖥️ Frontend React & OpenTelemetry

Le frontend constitue la couche de présentation de l’application et permet aux utilisateurs d’interagir avec les fonctionnalités exposées par le backend. Il offre une interface web simple simulant des usages concrets (consultation, actions utilisateur, navigation), tout en jouant un rôle central dans la génération des traces.  
Chaque interaction déclenchée côté interface entraîne une cascade d’appels vers l’API backend, ce qui permet d’observer et d’analyser le chemin d’une requête, depuis l’action utilisateur, dans un contexte d’instrumentation OpenTelemetry.

### Emplacement
```
Frontend/react/
```

### Fonctionnalités
- Interface utilisateur complète
- Consommation de l’API REST
- Instrumentation automatique des appels HTTP (fetch)

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

Dans ce projet, le traçage est utilisé pour observer le déroulement des scénarios d’exécution et l’enchaînement des opérations au sein des composants instrumentés. Les traces produites ne couvrent pas l’intégralité du chemin de bout en bout entre le frontend et le backend, mais offrent néanmoins une vision structurée des interactions et des traitements réalisés, suffisante pour analyser les comportements et illustrer les principes d’observabilité.


### OpenTelemetry Collector
- Configuration : `otelcol.yaml`
- Réception OTLP
- Traitement batch
- Export vers Jaeger

### Jaeger

Jaeger fournit une interface de visualisation des traces collectées, facilitant l’analyse des scénarios exécutés et des opérations instrumentées. Les traces observées reflètent les enchaînements d’appels et les durées associées, et sont utilisées comme support d’analyse.


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
