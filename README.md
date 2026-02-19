# MasterAnnonce — API REST Java (TP Dev Avancé #3)

API REST professionnelle de gestion d'annonces, développée **sans Spring** avec JAX-RS (Jersey), JPA (Hibernate), et une authentification JAAS stateless par token Bearer.

---

## Table des matières

1. [Architecture](#architecture)
2. [Stack technique](#stack-technique)
3. [Endpoints de l'API](#endpoints-de-lapi)
4. [Authentification JAAS](#authentification-jaas)
5. [Règles métier](#règles-métier)
6. [Prérequis](#prérequis)
7. [Build & Run](#build--run)
8. [Tests](#tests)
9. [Problèmes rencontrés et solutions](#problèmes-rencontrés-et-solutions)
10. [Structure du projet](#structure-du-projet)

---

## Architecture

Architecture en couches stricte (aucune dépendance circulaire) :

```
Resource (JAX-RS)  →  Service  →  Repository (DAO)  →  JPA / Hibernate  →  PostgreSQL
    ↕                    ↕
  DTO (in/out)    Business Rules
    ↕
ExceptionMapper ← BusinessException
    ↕
SecurityFilter (JAAS) → TokenStore
```

**Couche Resource** : expose les endpoints REST, valide les entrées (Bean Validation), délègue au Service.
**Couche Service** : contient toute la logique métier (ownership, workflow de statut, optimistic locking), gère les transactions.
**Couche Repository** : requêtes JPQL paginées avec `JOIN FETCH` pour éviter le LazyInitializationException.
**Couche Sécurité** : JAAS avec `@NameBinding` (`@Secured`) — pas de filtre Servlet, pas de Spring Security.

---

## Stack technique

| Composant          | Technologie                        | Version  |
|--------------------|------------------------------------|----------|
| Runtime            | Java                               | 11       |
| Build              | Maven (WAR)                        | 3.x      |
| REST               | JAX-RS / Jersey                    | 2.34     |
| DI                 | HK2                                | 2.34     |
| Persistence        | JPA / Hibernate                    | 5.6.15   |
| Validation         | Hibernate Validator                | 6.2.5    |
| BDD production     | PostgreSQL                         | 42.7.2   |
| BDD test           | H2 in-memory                       | 2.2.224  |
| Sécurité           | JAAS (LoginModule custom)          | JDK      |
| Documentation API  | Swagger / OpenAPI 3                | 2.2.8    |
| Logging            | SLF4J + Logback                    | 1.7 / 1.2 |
| Tests              | JUnit 5 + Mockito 5               | 5.9 / 5.2 |
| Serveur            | Apache Tomcat (ou tout conteneur Servlet 4.0+) | 9.x |

---

## Endpoints de l'API

Base URL : `http://localhost:8080/MasterAnnonce-1.0-SNAPSHOT/api`

### Test

| Méthode | Chemin               | Description                           | Auth |
|---------|----------------------|---------------------------------------|------|
| GET     | `/helloWorld`        | Test de connectivité                  | Non  |
| GET     | `/params/{id}`       | Démo PathParam + QueryParams          | Non  |
| GET     | `/params`            | Démo QueryParams seuls                | Non  |

### Authentification

| Méthode | Chemin    | Description                              | Auth |
|---------|-----------|------------------------------------------|------|
| POST    | `/login`  | Authentification → retourne un token JWT | Non  |

**Body** : `{ "username": "admin", "password": "admin123" }`
**Réponse** : `{ "token": "uuid-token", "expiresIn": 3600 }`

### Annonces (CRUD)

| Méthode | Chemin            | Description                          | Auth   |
|---------|-------------------|--------------------------------------|--------|
| GET     | `/annonces`       | Liste paginée (keyword, categoryId, status, page, pageSize) | Non |
| GET     | `/annonces/{id}`  | Détail d'une annonce                 | Non    |
| POST    | `/annonces`       | Créer une annonce                    | **Oui** |
| PUT     | `/annonces/{id}`  | Mise à jour complète                 | **Oui** |
| PATCH   | `/annonces/{id}`  | Mise à jour partielle (+ changement de statut) | **Oui** |
| DELETE  | `/annonces/{id}`  | Supprimer (si ARCHIVED)              | **Oui** |

### Swagger / OpenAPI

| Méthode | Chemin             | Description              |
|---------|--------------------|--------------------------|
| GET     | `/openapi.json`    | Spécification OpenAPI 3  |

---

## Authentification JAAS

L'authentification est **stateless** basée sur un token Bearer en mémoire.

### Flux de connexion

```
Client                          Server
  │                               │
  │  POST /api/login              │
  │  { username, password }       │
  │ ─────────────────────────────>│
  │                               │── JAAS LoginContext("MasterAnnonceLogin")
  │                               │   └── DbLoginModule.login()
  │                               │       └── UserRepository.findByUsername()
  │                               │       └── Vérifie mot de passe
  │                               │       └── Ajoute UserPrincipal + RolePrincipal au Subject
  │                               │── Génère UUID token → TokenStore.store()
  │  { token, expiresIn: 3600 }  │
  │ <─────────────────────────────│
  │                               │
  │  GET /api/annonces (POST,etc.)│
  │  Authorization: Bearer <token>│
  │ ─────────────────────────────>│
  │                               │── @Secured filter intercepts
  │                               │── JAAS LoginContext("MasterAnnonceToken")
  │                               │   └── TokenLoginModule.login()
  │                               │       └── TokenStore.validate(token)
  │                               │       └── Reconstruit UserPrincipal + RolePrincipal
  │                               │── SecurityContext → resource method
  │  200 OK { ... }               │
  │ <─────────────────────────────│
```

### Modules JAAS (jaas.conf)

```
MasterAnnonceLogin {
    org.univ_paris8.iut.montreuil...security.module.DbLoginModule required;
};

MasterAnnonceToken {
    org.univ_paris8.iut.montreuil...security.module.TokenLoginModule required;
};
```

La configuration est chargée automatiquement au démarrage par `AppInitializer` (ServletContextListener) via `java.security.auth.login.config`.

---

## Règles métier

### Workflow de statut

```
DRAFT  ──[PATCH status]──>  PUBLISHED  ──[PATCH status]──>  ARCHIVED
```

- Une annonce est créée avec le statut **DRAFT**.
- Seul l'**auteur** peut modifier ou supprimer ses annonces (**ForbiddenException** 403 sinon).
- Une annonce **PUBLISHED** ne peut plus être modifiée (contenu) → **ConflictException** 409.
- Seule une annonce **ARCHIVED** peut être supprimée → **ConflictException** 409 sinon.
- **Optimistic locking** via `@Version` : si la version envoyée ne correspond pas, **ConflictException** 409.

### Gestion des erreurs normalisée

Toutes les erreurs retournent un format JSON uniforme :

```json
{
  "error": "NOT_FOUND",
  "messages": ["Annonce avec l'id 42 introuvable"]
}
```

| HTTP | Type               | Cas d'usage                        |
|------|--------------------|------------------------------------|
| 400  | VALIDATION_ERROR   | Bean Validation (champs invalides) |
| 401  | UNAUTHORIZED       | Token manquant ou expiré           |
| 403  | FORBIDDEN          | Action interdite (pas l'auteur)    |
| 404  | NOT_FOUND          | Ressource introuvable              |
| 409  | CONFLICT           | Règle métier / Optimistic Lock     |
| 500  | INTERNAL_ERROR     | Erreur serveur imprévue            |

---

## Prérequis

- **Java 11+** (JDK)
- **Maven 3.6+**
- **PostgreSQL 14+** (port 5434, base `MasterAnnonce`, user `postgres`, password `password13`)
- **Tomcat 9+** (ou autre conteneur Servlet 4.0+)

### Initialisation de la base

```bash
psql -U postgres -p 5434 -f sql/init.sql
```

Le script `sql/init.sql` crée les tables, les contraintes et insère des données de démonstration.

> **Note** : Hibernate `hbm2ddl.auto=update` peut aussi créer les tables automatiquement au premier démarrage.

---

## Build & Run

### Compilation

```bash
# Build complet (compile + tests unitaires)
mvn clean package

# Build sans tests
mvn clean package -DskipTests
```

### Déploiement

Copier `target/MasterAnnonce-1.0-SNAPSHOT.war` dans le dossier `webapps/` de Tomcat, ou utiliser le plugin Maven :

```bash
# Avec le plugin Tomcat Maven (si configuré)
mvn tomcat7:run
```

### URL de base

```
http://localhost:8080/MasterAnnonce-1.0-SNAPSHOT/api/
```

---

## Tests

### Tests unitaires

```bash
mvn test -Punit-tests
```

Exécute via **Maven Surefire** les tests `*Test.java` (excluant `*IntegrationTest` et `*IT`).

### Tests d'intégration

```bash
mvn verify -Pintegration
```

Exécute via **Maven Failsafe** les tests `*IntegrationTest.java` et `*IT.java` avec H2 en mémoire.

### Couverture

| Package    | Classe de test                       | Type        | Description |
|------------|--------------------------------------|-------------|-------------|
| service    | `AnnonceServiceTest`                 | Unitaire    | Mock des repos, règles métier, ownership, workflow statuts |
| service    | `AnnonceIntegrationTest`             | Intégration | CRUD complet avec H2, lifecycle DRAFT→PUBLISHED→ARCHIVED→DELETE |
| dao        | `AnnonceRepositoryIntegrationTest`   | Intégration | Pagination, filtres keyword/category/status, CRUD JPA |
| resource   | `AnnonceResourceTest`                | Unitaire    | Mock du service, validation des réponses HTTP |
| security   | `JaasModuleTest`                     | Unitaire    | TokenStore et TokenLoginModule |
| security   | `AuthenticationFilterTest`           | Unitaire    | JaasAuthenticationFilter (401 sans token, etc.) |

---

## Problèmes rencontrés et solutions

### 1. LazyInitializationException

**Problème** : L'accès à `annonce.category.label` hors transaction provoquait un crash (session Hibernate fermée).

**Solution** : Utilisation systématique de `JOIN FETCH` dans les requêtes JPQL du Repository :
```java
SELECT a FROM Annonce a LEFT JOIN FETCH a.category LEFT JOIN FETCH a.author WHERE a.id = :id
```

### 2. Gestion des transactions dans la couche Service

**Problème** : Incohérence des données lors de la création d'une annonce (relations user + category).

**Solution** : Centralisation des transactions dans le Service avec `EntityManagerHelper` (ThreadLocal). Pattern begin/commit/rollback explicite avec bloc finally.

### 3. Configuration JAAS dynamique

**Problème** : Le fichier `jaas.conf` doit être accessible au runtime, mais son chemin varie entre IDE et Tomcat.

**Solution** : Le `AppInitializer` (ServletContextListener) charge automatiquement la config JAAS depuis le classpath via `getClass().getClassLoader().getResource("jaas.conf")` et configure `java.security.auth.login.config` au démarrage.

### 4. Sérialisation des entités JPA (références circulaires)

**Problème** : Jackson échouait à sérialiser les entités avec des relations bidirectionnelles (User ↔ Annonce).

**Solution** : Introduction de DTOs (Data Transfer Objects) avec un pattern Builder. Le `AnnonceMapper` convertit Annonce → AnnonceDTO en extrayant uniquement les champs nécessaires, cassant les cycles.

### 5. Optimistic Locking et concurrence

**Problème** : Deux utilisateurs modifiant la même annonce simultanément pouvaient écraser les changements de l'autre.

**Solution** : Champ `@Version` sur l'entité Annonce. Le client doit envoyer la version actuelle dans chaque PUT/PATCH. `OptimisticLockException` est capturée et transformée en `ConflictException` (409).

### 6. Bean Validation avec Jersey

**Problème** : Jersey renvoie des erreurs de validation dans un format par défaut peu exploitable.

**Solution** : Désactivation de `ServerProperties.BV_SEND_ERROR_IN_RESPONSE` dans `JaxRsApplication` et création d'un `ValidationExceptionMapper` qui transforme les `ConstraintViolationException` en format normalisé.

### 7. Jersey proxy SecurityContext et extraction de l'identité

**Problème** : Après une authentification JAAS réussie, le filtre `JaasAuthenticationFilter` posait correctement un `UserSecurityContext` personnalisé via `requestContext.setSecurityContext(...)`. Cependant, dans la méthode `createAnnonce` du `AnnonceResource`, le test `securityContext instanceof UserSecurityContext` retournait toujours `false`, provoquant une `NotAuthorizedException` (erreur 500) alors que le token était valide et l'utilisateur authentifié.

**Cause racine** : Jersey injecte un **objet proxy** dans les paramètres annotés `@Context SecurityContext`. Ce proxy délègue les appels de méthodes vers le vrai `UserSecurityContext`, mais le `instanceof` échoue car le proxy n'est pas une instance directe de la classe custom.

**Solution** : Au lieu de tester le type du `SecurityContext`, on teste le type du `Principal` retourné par `getUserPrincipal()` — Jersey délègue correctement cet appel à travers le proxy :

```java
// AVANT (KO) — le proxy Jersey ne passe pas le instanceof
if (securityContext instanceof UserSecurityContext) {
    return ((UserSecurityContext) securityContext).getUserId();
}

// APRÈS (OK) — on teste le Principal, pas le wrapper
if (securityContext.getUserPrincipal() instanceof UserPrincipal) {
    return ((UserPrincipal) securityContext.getUserPrincipal()).getUserId();
}
```

---

## Structure du projet

```
src/
├── main/
│   ├── java/.../masterannonce/
│   │   ├── config/
│   │   │   └── JaxRsApplication.java          # JAX-RS ResourceConfig
│   │   ├── dto/
│   │   │   ├── AnnonceDTO.java                 # Response DTO (Builder)
│   │   │   ├── AnnonceCreateDTO.java           # Create request
│   │   │   ├── AnnonceUpdateDTO.java           # Full update request
│   │   │   ├── AnnoncePatchDTO.java            # Partial update request
│   │   │   ├── AnnonceMapper.java              # Entity ↔ DTO conversion
│   │   │   ├── LoginRequestDTO.java            # Login request
│   │   │   ├── LoginResponseDTO.java           # Login response (token)
│   │   │   ├── ErrorResponseDTO.java           # Normalized error format
│   │   │   └── PaginatedResponseDTO.java       # Generic paginated response
│   │   ├── exception/
│   │   │   ├── BusinessException.java          # Base exception
│   │   │   ├── NotFoundException.java          # 404
│   │   │   ├── ForbiddenException.java         # 403
│   │   │   ├── ConflictException.java          # 409
│   │   │   ├── BusinessExceptionMapper.java    # JAX-RS ExceptionMapper
│   │   │   ├── ValidationExceptionMapper.java  # Bean Validation errors
│   │   │   └── GenericExceptionMapper.java     # Catch-all 500
│   │   ├── model/
│   │   │   ├── Annonce.java                    # JPA entity (@Version)
│   │   │   ├── User.java                       # JPA entity (+ role)
│   │   │   ├── Category.java                   # JPA entity
│   │   │   └── AnnonceStatus.java              # Enum DRAFT/PUBLISHED/ARCHIVED
│   │   ├── dao/
│   │   │   ├── AnnonceRepository.java          # JPQL with pagination + JOIN FETCH
│   │   │   ├── UserRepository.java             # findById, findByUsername
│   │   │   └── CategoryRepository.java         # findById, findAll
│   │   ├── service/
│   │   │   ├── AnnonceService.java             # Business rules + transactions
│   │   │   └── UserService.java                # JAAS login flow
│   │   ├── resource/
│   │   │   ├── HelloWorldResource.java         # GET /helloWorld
│   │   │   ├── ParamsResource.java             # GET /params demo
│   │   │   ├── AnnonceResource.java            # Full CRUD /annonces
│   │   │   └── LoginResource.java              # POST /login
│   │   ├── security/
│   │   │   ├── Secured.java                    # @NameBinding annotation
│   │   │   ├── TokenStore.java                 # In-memory token storage
│   │   │   ├── UserSecurityContext.java         # JAX-RS SecurityContext impl
│   │   │   ├── LoginCallbackHandler.java       # JAAS callback (login)
│   │   │   ├── TokenCallbackHandler.java       # JAAS callback (token)
│   │   │   ├── filter/
│   │   │   │   └── JaasAuthenticationFilter.java  # @Secured filter
│   │   │   ├── module/
│   │   │   │   ├── DbLoginModule.java          # JAAS: DB auth
│   │   │   │   └── TokenLoginModule.java       # JAAS: Token validation
│   │   │   └── principal/
│   │   │       ├── UserPrincipal.java
│   │   │       └── RolePrincipal.java
│   │   ├── listener/
│   │   │   └── AppInitializer.java             # ServletContextListener
│   │   └── utils/
│   │       └── EntityManagerHelper.java        # ThreadLocal EM management
│   └── resources/
│       ├── META-INF/persistence.xml            # PostgreSQL config
│       ├── jaas.conf                           # JAAS LoginModule config
│       └── logback.xml                         # Logging config
├── test/
│   ├── java/.../masterannonce/
│   │   ├── service/
│   │   │   ├── AnnonceServiceTest.java         # Unit tests (Mockito)
│   │   │   └── AnnonceIntegrationTest.java     # H2 integration tests
│   │   ├── dao/
│   │   │   └── AnnonceRepositoryIntegrationTest.java  # H2 repo tests
│   │   ├── resource/
│   │   │   └── AnnonceResourceTest.java        # Unit tests (Mockito)
│   │   └── security/
│   │       ├── JaasModuleTest.java             # TokenStore/Module tests
│   │       └── AuthenticationFilterTest.java   # Filter tests
│   └── resources/
│       └── META-INF/persistence.xml            # H2 in-memory config
├── sql/
│   └── init.sql                                # PostgreSQL DDL + seed data
└── postman/
    └── MasterAnnonce.postman_collection.json   # Collection Postman
```
