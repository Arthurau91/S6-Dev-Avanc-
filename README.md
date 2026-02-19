# MasterAnnonce — API REST Spring Boot (TP Dev Avancé)

API REST professionnelle de gestion d'annonces, développée avec **Spring Boot 3.2**, Spring Security (JWT), Spring Data JPA, MapStruct, et déployable via Docker / Kubernetes.

---

## Table des matières

1. [Architecture](#architecture)
2. [Stack technique](#stack-technique)
3. [Endpoints de l'API](#endpoints-de-lapi)
4. [Authentification JWT](#authentification-jwt)
5. [Règles métier](#règles-métier)
6. [Prérequis](#prérequis)
7. [Build & Run](#build--run)
8. [Docker & Kubernetes](#docker--kubernetes)
9. [Tests](#tests)
10. [Fonctionnalités avancées](#fonctionnalités-avancées)
11. [Problèmes rencontrés et solutions](#problèmes-rencontrés-et-solutions)
12. [Structure du projet](#structure-du-projet)

---

## Architecture

Architecture en couches stricte (aucune dépendance circulaire) :

```
Controller (Spring MVC)  →  Service  →  Repository (Spring Data JPA)  →  PostgreSQL
      ↕                       ↕
  DTO (in/out)         Business Rules
  MapStruct                   ↕
      ↕                 Specifications
GlobalExceptionHandler  ←  BusinessException
      ↕
JwtAuthenticationFilter  →  JwtTokenProvider
      ↕
CorrelationIdFilter (MDC)
      ↕
LoggingAspect (AOP)
```

**Couche Controller** : expose les endpoints REST, valide les entrées (Bean Validation), délègue au Service.
**Couche Service** : contient toute la logique métier (ownership, workflow de statut, optimistic locking), gère les transactions via `@Transactional`.
**Couche Repository** : interfaces Spring Data JPA avec `JpaSpecificationExecutor` pour les requêtes dynamiques.
**Couche Sécurité** : Spring Security stateless avec JWT (jjwt) — filtre `OncePerRequestFilter`, `UserPrincipal` custom.
**Couche AOP** : `LoggingAspect` pour le logging transversal de toutes les méthodes Service (entrée, sortie, durée, exceptions).
**Couche Config** : `CorrelationIdFilter` (traçabilité), `DataInitializer` (seed), `OpenApiConfig` (Swagger), `SecurityConfig`.

---

## Stack technique

| Composant          | Technologie                        | Version    |
|--------------------|------------------------------------|------------|
| Runtime            | Java                               | 17         |
| Framework          | Spring Boot                        | 3.2.5      |
| Build              | Maven (JAR exécutable)             | 3.x        |
| REST               | Spring Web (Spring MVC)            | 6.x        |
| DI                 | Spring IoC                         | 6.x        |
| Persistence        | Spring Data JPA / Hibernate        | 6.x        |
| Requêtes dynamiques| JPA Specifications                 | —          |
| Mapping DTO        | MapStruct                          | 1.5.5      |
| Validation         | Jakarta Bean Validation            | 3.x        |
| BDD production     | PostgreSQL                         | 15 (Alpine)|
| BDD test           | Testcontainers (PostgreSQL)        | 1.19.7     |
| Sécurité           | Spring Security + JWT (jjwt)       | 6.x / 0.12.5 |
| Documentation API  | SpringDoc OpenAPI 3 (Swagger UI)   | 2.5.0      |
| Logging            | SLF4J + Logback + MDC              | —          |
| AOP                | Spring AOP (AspectJ)               | 6.x        |
| Tests              | JUnit 5 + Mockito + MockMvc        | 5.x        |
| Conteneurisation   | Docker (multi-stage) + Docker Compose | —       |
| Orchestration      | Kubernetes (Minikube)              | —          |
| Monitoring         | Spring Boot Actuator               | 3.2.5      |
| Couverture         | JaCoCo                             | 0.8.12     |
| Configuration      | `spring.config.import` (natif Spring Boot) | 3.2.5    |

---

## Endpoints de l'API

Base URL : `http://localhost:8080`

### Authentification

| Méthode | Chemin             | Description                              | Auth |
|---------|--------------------|------------------------------------------|------|
| POST    | `/api/auth/login`  | Authentification → retourne un token JWT | Non  |

**Body** : `{ "username": "admin", "password": "admin123" }`
**Réponse** : `{ "token": "eyJhbGciOi...", "expiresIn": 3600 }`

### Annonces (CRUD)

| Méthode | Chemin              | Description                                         | Auth   |
|---------|---------------------|-----------------------------------------------------|--------|
| GET     | `/api/annonces`     | Liste paginée (q, status, categoryId, authorId, fromDate, toDate, page, size, sortBy, sortDir) | Non |
| GET     | `/api/annonces/{id}`| Détail d'une annonce                                | Non    |
| POST    | `/api/annonces`     | Créer une annonce                                   | **Oui** |
| PUT     | `/api/annonces/{id}`| Mise à jour complète                                | **Oui** |
| PATCH   | `/api/annonces/{id}`| Mise à jour partielle (+ changement de statut)      | **Oui** |
| DELETE  | `/api/annonces/{id}`| Supprimer (si ARCHIVED)                             | **Oui** |

### Meta (Introspection)

| Méthode | Chemin              | Description                                               | Auth |
|---------|---------------------|-----------------------------------------------------------|------|
| GET     | `/api/meta/annonces`| Champs filtrables/triables via Reflection (introspection) | Non  |

### Swagger / OpenAPI

| Méthode | Chemin              | Description              |
|---------|---------------------|--------------------------|
| GET     | `/swagger-ui.html`  | Interface Swagger UI     |
| GET     | `/v3/api-docs`      | Spécification OpenAPI 3  |

### Actuator

| Méthode | Chemin                | Description              |
|---------|-----------------------|--------------------------|
| GET     | `/actuator/health`    | Health check             |
| GET     | `/actuator/info`      | Informations application |

---

## Authentification JWT

L'authentification est **stateless** basée sur un token JWT signé (HMAC-SHA).

### Flux de connexion

```
Client                              Server
  │                                   │
  │  POST /api/auth/login             │
  │  { username, password }           │
  │ ─────────────────────────────────>│
  │                                   │── AuthService.login()
  │                                   │   └── UserRepository.findByUsername()
  │                                   │   └── BCryptPasswordEncoder.matches()
  │                                   │   └── JwtTokenProvider.generateToken()
  │                                   │       └── Claims: userId, username, role
  │  { token: "eyJ...", expiresIn }   │
  │ <─────────────────────────────────│
  │                                   │
  │  POST /api/annonces               │
  │  Authorization: Bearer <jwt>      │
  │ ─────────────────────────────────>│
  │                                   │── JwtAuthenticationFilter (OncePerRequestFilter)
  │                                   │   └── JwtTokenProvider.validateToken(jwt)
  │                                   │   └── Extrait userId, username, role
  │                                   │   └── Crée UserPrincipal + Authentication
  │                                   │   └── SecurityContextHolder.setAuthentication()
  │                                   │── Controller → Service → Repository
  │  201 Created { ... }              │
  │ <─────────────────────────────────│
```

### Configuration JWT (application.yml)

```yaml
jwt:
  secret: ${JWT_SECRET:YTJiM2M0ZDVlNmY3ZzhoOWkwajFrMmwzbTRuNW82cDdxOHI5czB0MXUydjN3NHg1eTZ6}
  expiration-ms: ${JWT_EXPIRATION_MS:3600000}   # 1 heure
```

> Les variables JWT sont également chargées depuis le fichier `.env` via `spring.config.import`.

---

## Règles métier

### Workflow de statut

```
DRAFT  ──[PATCH status]──>  PUBLISHED  ──[PATCH status (ADMIN)]──>  ARCHIVED
```

- Une annonce est créée avec le statut **DRAFT**.
- Seul l'**auteur** peut modifier ou supprimer ses annonces (**ForbiddenException** 403 sinon).
- Une annonce **PUBLISHED** ne peut plus être modifiée (contenu) — seul le changement de statut vers ARCHIVED est autorisé → **ConflictException** 409.
- Seul un **ADMIN** peut archiver une annonce (**ForbiddenException** 403 sinon).
- Seule une annonce **ARCHIVED** peut être supprimée → **ConflictException** 409 sinon.
- **Optimistic locking** via `@Version` : si la version envoyée ne correspond pas, **ConflictException** 409.

### Gestion des erreurs normalisée

Toutes les erreurs retournent un format JSON uniforme via `GlobalExceptionHandler` (`@RestControllerAdvice`) :

```json
{
  "error": "NOT_FOUND",
  "messages": ["Annonce avec l'id 42 non trouvée"]
}
```

| HTTP | Type               | Cas d'usage                         |
|------|--------------------|-------------------------------------|
| 400  | VALIDATION_ERROR   | Bean Validation (champs invalides)  |
| 400  | BAD_REQUEST        | Argument illégal (tri invalide)     |
| 401  | UNAUTHORIZED       | Token manquant, expiré ou invalide  |
| 403  | FORBIDDEN          | Action interdite (pas l'auteur/admin)|
| 404  | NOT_FOUND          | Ressource introuvable               |
| 409  | CONFLICT           | Règle métier / Optimistic Lock      |
| 500  | INTERNAL_ERROR     | Erreur serveur imprévue             |

---

## Prérequis

- **Java 17+** (JDK)
- **Maven 3.6+**
- **Docker & Docker Compose** (pour le déploiement conteneurisé)
- **PostgreSQL 14+** (si exécution locale sans Docker)

### Configuration via `.env`

Les paramètres de la base de données, du JWT et de Hibernate sont externalisés dans un fichier **`.env`** à la racine du projet (non versionné).

L'application utilise la fonctionnalité native **`spring.config.import`** (Spring Boot 2.4+) pour charger automatiquement le fichier `.env` comme fichier de propriétés au démarrage. Cela fonctionne aussi bien **en exécution locale** (`mvn spring-boot:run` ou `java -jar`) qu'**en Docker Compose**.

Dans `application.yml`, l'import est déclaré ainsi :

```yaml
spring:
  config:
    import: optional:file:.env[.properties]

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/MasterAnnonce}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:password}
```

> **Fonctionnement** : `optional:file:.env[.properties]` charge le fichier `.env` à la racine du projet comme un fichier `.properties`. Le mot-clé `optional` évite une erreur si le fichier est absent — les valeurs par défaut (après le `:`) sont alors utilisées. En Docker Compose, les variables d'environnement système prennent la priorité.

1. Copier le template :

```bash
cp .env.example .env
```

2. Éditer `.env` avec vos valeurs :

```dotenv
# Database
POSTGRES_DB=MasterAnnonce
POSTGRES_USER=postgres
POSTGRES_PASSWORD=changeme
POSTGRES_PORT=5434

# Spring datasource (used by the app container)
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/MasterAnnonce
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=changeme

# JPA / Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# JWT
JWT_SECRET=changeme_generate_a_base64_secret
JWT_EXPIRATION_MS=3600000
```

> **⚠️ Sécurité** : le fichier `.env` est ajouté au `.gitignore` et ne doit **jamais** être commité. Seul `.env.example` (sans secrets réels) est versionné.

### Initialisation de la base

```bash
psql -U $POSTGRES_USER -p $POSTGRES_PORT -f sql/init.sql
```

Le script `sql/init.sql` crée les tables, les index et insère des données de démonstration.

> **Note** : `DataInitializer` (CommandLineRunner) insère automatiquement les catégories et utilisateurs au démarrage si la base est vide. Les mots de passe sont hashés en BCrypt.

---

## Build & Run

### Compilation

```bash
# Build complet (compile + tests unitaires)
mvn clean package

# Build sans tests
mvn clean package -DskipTests
```

### Exécution locale

> **Important** : Avant de lancer l'application, assurez-vous d'avoir un fichier `.env` à la racine du projet avec vos paramètres de connexion BDD. L'application utilise `spring-dotenv` pour charger automatiquement ces variables.

```bash
# 1. Configurer le .env (une seule fois)
cp .env.example .env
# Éditer .env avec vos valeurs réelles...

# 2. Lancer l'application (nécessite PostgreSQL accessible)
java -jar target/MasterAnnonce-1.0-SNAPSHOT.jar

# Ou via Maven
mvn spring-boot:run
```

### URL de base

```
http://localhost:8080
```

---

## Docker & Kubernetes

### Docker Compose

```bash
# Copier et configurer le fichier d'environnement (une seule fois)
cp .env.example .env
# Éditer .env avec vos valeurs réelles...

# Démarrer l'application + PostgreSQL
docker-compose up -d --build

# Vérifier les logs
docker-compose logs -f app

# Arrêter
docker-compose down
```

Le `Dockerfile` utilise un **multi-stage build** (Eclipse Temurin 17) :
1. **Stage Build** : compile avec Maven (cache des dépendances)
2. **Stage Runtime** : image JRE minimale, utilisateur non-root, healthcheck Actuator

### Kubernetes (Minikube)

Manifestes disponibles dans `k8s/` :

| Fichier                   | Description                              |
|---------------------------|------------------------------------------|
| `postgres-secret.yml`     | Credentials PostgreSQL (Secret)          |
| `postgres-pvc.yml`        | Volume persistant pour PostgreSQL        |
| `postgres-deployment.yml` | Deployment + Service PostgreSQL          |
| `app-configmap.yml`       | Configuration applicative (ConfigMap)    |
| `app-secret.yml`          | JWT secret + DB password (Secret)        |
| `app-deployment.yml`      | Deployment (2 replicas) + Service (ClusterIP) |
| `app-ingress.yml`         | Ingress pour accès externe               |

```bash
# Déployer sur Minikube
eval $(minikube docker-env)
docker build -t masterannonce:latest .
kubectl apply -f k8s/
```

---

## Tests

### Tests unitaires

```bash
mvn test
```

Exécute via **Maven Surefire** les tests `*Test.java` (JUnit 5 + Mockito).

### Tests d'intégration

```bash
mvn verify
```

Exécute via **Maven Failsafe** les tests `*IT.java` avec **Testcontainers** (PostgreSQL réel dans Docker).

### Couverture de code (JaCoCo)

```bash
mvn verify
# Rapport HTML : target/site/jacoco/index.html
```

### Détail des tests

| Package     | Classe de test              | Type        | Description |
|-------------|-----------------------------|-------------|-------------|
| service     | `AnnonceServiceTest`        | Unitaire    | Mock des repos (Mockito), règles métier, ownership, workflow statuts, Specifications |
| integration | `AnnonceIntegrationIT`      | Intégration | CRUD complet avec Testcontainers (PostgreSQL), login JWT, protection 401/403, lifecycle DRAFT→PUBLISHED→ARCHIVED→DELETE |

---

## Fonctionnalités avancées

### 1. Requêtes dynamiques — JPA Specifications

La recherche d'annonces utilise le pattern **JPA Specifications** (`AnnonceSpecifications`) pour composer dynamiquement les critères de filtrage :

```java
Specification<Annonce> spec = Specification.where(null);
if (keyword != null) spec = spec.and(AnnonceSpecifications.hasKeyword(keyword));
if (status != null)  spec = spec.and(AnnonceSpecifications.hasStatus(status));
// ...composable à l'infini
```

Filtres disponibles : `q` (keyword titre/description), `status`, `categoryId`, `authorId`, `fromDate`, `toDate`.

### 2. Mapping DTO — MapStruct

Le mapping Entity ↔ DTO est géré par **MapStruct** (génération de code à la compilation) :

- `toDTO()` : Annonce → AnnonceDTO (aplatissement des relations)
- `toEntity()` : AnnonceCreateDTO → Annonce
- `updateEntityFromDTO()` : AnnonceUpdateDTO → Annonce existante (`@MappingTarget`)
- `patchEntityFromDTO()` : AnnoncePatchDTO → Annonce existante (`NullValuePropertyMappingStrategy.IGNORE`)

### 3. AOP — Logging transversal

`LoggingAspect` intercepte toutes les méthodes de la couche Service via un pointcut :

- **Entrée** : nom de méthode + arguments (sanitisés — pas de mot de passe/token)
- **Sortie** : durée d'exécution en ms
- **Exception** : type + message
- **Protection LazyLoading** : les entités JPA ne sont pas sérialisées dans les logs

### 4. Correlation ID

`CorrelationIdFilter` génère ou propage un UUID unique par requête (`X-Correlation-Id`). Stocké dans le MDC (Mapped Diagnostic Context) pour traçabilité dans les logs.

### 5. Introspection par Reflection

- **MetaController** (`GET /api/meta/annonces`) : expose dynamiquement les champs de l'entité `Annonce` (nom, type) via `java.lang.reflect`.
- **AnnonceController** : valide dynamiquement le champ de tri (`sortBy`) contre les champs réels de l'entité via Reflection.

### 6. Spring Boot Actuator

Endpoints de monitoring exposés :
- `/actuator/health` — état de l'application et de la BDD
- `/actuator/info` — métadonnées de l'application

---

## Problèmes rencontrés et solutions

### 1. Migration JAX-RS → Spring Boot

**Problème** : L'architecture initiale (JAX-RS/Jersey, JAAS, HK2, EntityManagerHelper ThreadLocal) était complexe et difficile à maintenir.

**Solution** : Migration complète vers Spring Boot 3.2 avec Spring Security (JWT), Spring Data JPA, et injection de dépendances native Spring. Simplification massive de la gestion des transactions (`@Transactional`), de la sécurité et du DI.

### 2. LazyInitializationException

**Problème** : L'accès à `annonce.category.label` hors session Hibernate provoquait un crash.

**Solution** : Utilisation de MapStruct avec `@Mapping(source = "author.username", target = "authorUsername")` — les relations sont résolues dans la couche transactionnelle du Service avant conversion en DTO. `spring.jpa.open-in-view=false` est explicitement désactivé.

### 3. Sérialisation des entités JPA (références circulaires)

**Problème** : Jackson échouait à sérialiser les entités avec des relations bidirectionnelles (User ↔ Annonce).

**Solution** : Introduction de DTOs avec MapStruct. Le mapper extrait uniquement les champs nécessaires, cassant les cycles. Aucune entité JPA n'est jamais exposée directement dans les réponses REST.

### 4. Optimistic Locking et concurrence

**Problème** : Deux utilisateurs modifiant la même annonce simultanément pouvaient écraser les changements de l'autre.

**Solution** : Champ `@Version` sur l'entité Annonce. `ObjectOptimisticLockingFailureException` est capturée par le `GlobalExceptionHandler` et transformée en réponse 409 CONFLICT.

### 5. Validation des champs de tri par Reflection

**Problème** : Permettre un tri sur n'importe quel champ exposait des risques d'injection ou d'erreurs.

**Solution** : Introspection des champs de l'entité `Annonce` via `Annonce.class.getDeclaredFields()` dans un `static {}` block du contrôleur. Le champ `sortBy` est validé contre cette liste blanche avant utilisation.

### 6. Logging sécurisé avec AOP

**Problème** : Logger les arguments des méthodes Service pouvait exposer des mots de passe/tokens ou déclencher un LazyLoading.

**Solution** : `LoggingAspect.sanitizeArgs()` détecte les DTOs sensibles (Login, Password, Token) et les entités JPA, remplaçant leur affichage par des placeholders (`{***}`, `{id=?}`).

### 7. Tests d'intégration avec base réelle

**Problème** : Les tests avec H2 ne reflétaient pas le comportement réel de PostgreSQL (dialecte, types, contraintes).

**Solution** : Adoption de **Testcontainers** pour les tests d'intégration : un conteneur PostgreSQL réel est démarré automatiquement pour chaque suite de tests, garantissant la fidélité avec la production.

---

## Structure du projet

```
src/
├── main/
│   ├── java/.../masterannonce/
│   │   ├── MasterAnnonceApplication.java       # Point d'entrée Spring Boot
│   │   ├── aop/
│   │   │   └── LoggingAspect.java              # AOP: logging transversal Service
│   │   ├── config/
│   │   │   ├── CorrelationIdFilter.java        # Servlet Filter: X-Correlation-Id + MDC
│   │   │   ├── DataInitializer.java            # CommandLineRunner: seed BDD
│   │   │   ├── OpenApiConfig.java              # SpringDoc / Swagger configuration
│   │   │   └── SecurityConfig.java             # Spring Security (stateless JWT)
│   │   ├── controller/
│   │   │   ├── AnnonceController.java          # CRUD /api/annonces (+ tri par Reflection)
│   │   │   ├── AuthController.java             # POST /api/auth/login
│   │   │   └── MetaController.java             # GET /api/meta/annonces (introspection)
│   │   ├── dto/
│   │   │   ├── AnnonceDTO.java                 # Response DTO
│   │   │   ├── AnnonceCreateDTO.java           # Create request
│   │   │   ├── AnnonceUpdateDTO.java           # Full update request (PUT)
│   │   │   ├── AnnoncePatchDTO.java            # Partial update request (PATCH)
│   │   │   ├── AnnonceMapper.java              # MapStruct: Entity ↔ DTO
│   │   │   ├── LoginRequestDTO.java            # Login request
│   │   │   ├── LoginResponseDTO.java           # Login response (JWT token)
│   │   │   └── ErrorResponseDTO.java           # Format d'erreur normalisé
│   │   ├── exception/
│   │   │   ├── BusinessException.java          # Base exception (type + HTTP status)
│   │   │   ├── ResourceNotFoundException.java  # 404
│   │   │   ├── ForbiddenException.java         # 403
│   │   │   ├── ConflictException.java          # 409
│   │   │   └── GlobalExceptionHandler.java     # @RestControllerAdvice (catch-all)
│   │   ├── model/
│   │   │   ├── Annonce.java                    # JPA entity (@Version, @Index)
│   │   │   ├── User.java                       # JPA entity (BCrypt password)
│   │   │   ├── Category.java                   # JPA entity
│   │   │   └── AnnonceStatus.java              # Enum DRAFT / PUBLISHED / ARCHIVED
│   │   ├── repository/
│   │   │   ├── AnnonceRepository.java          # JpaRepository + JpaSpecificationExecutor
│   │   │   ├── UserRepository.java             # findByUsername
│   │   │   └── CategoryRepository.java         # CRUD catégories
│   │   ├── security/
│   │   │   ├── JwtAuthenticationFilter.java    # OncePerRequestFilter (Bearer token)
│   │   │   ├── JwtTokenProvider.java           # Génération / validation JWT (jjwt)
│   │   │   └── UserPrincipal.java              # Principal custom (userId + username)
│   │   ├── service/
│   │   │   ├── AnnonceService.java             # Logique métier + @Transactional
│   │   │   └── AuthService.java                # Authentification (BCrypt + JWT)
│   │   └── specification/
│   │       └── AnnonceSpecifications.java      # JPA Specifications (filtres dynamiques)
│   └── resources/
│       └── application.yml                     # Configuration Spring Boot (DB, JWT, Actuator, Logging)
├── test/
│   ├── java/.../masterannonce/
│   │   ├── service/
│   │   │   └── AnnonceServiceTest.java         # Tests unitaires (Mockito)
│   │   └── integration/
│   │       └── AnnonceIntegrationIT.java       # Tests intégration (Testcontainers + MockMvc)
│   └── resources/
│       └── application-test.yml                # Config test (create-drop, logging réduit)
├── sql/
│   └── init.sql                                # PostgreSQL DDL + seed data
├── k8s/                                        # Manifestes Kubernetes
│   ├── postgres-secret.yml
│   ├── postgres-pvc.yml
│   ├── postgres-deployment.yml
│   ├── app-configmap.yml
│   ├── app-secret.yml
│   ├── app-deployment.yml
│   └── app-ingress.yml
├── postman/
│   └── MasterAnnonce.postman_collection.json   # Collection Postman
├── .env                                        # Variables d'environnement (non versionné)
├── .env.example                                # Template .env (versionné, sans secrets)
├── docker-compose.yml                          # Docker Compose (app + PostgreSQL)
└── Dockerfile                                  # Multi-stage build (Temurin 17)
```
