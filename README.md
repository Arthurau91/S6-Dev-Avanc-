## Problèmes rencontrés et Solutions

Durant la migration, plusieurs problèmes techniques ont été rencontrés et résolus :

### 1. LazyInitializationException
*   **Problème :** Lors de l'affichage de la liste des annonces, l'accès à `annonce.category.label` provoquait un crash car la session Hibernate était déjà fermée (Pattern "Open Session in View" non utilisé ici).
*   **Solution :** Utilisation de `JOIN FETCH` dans les requêtes JPQL du Repository (`SELECT a FROM Annonce a LEFT JOIN FETCH a.category...`) pour charger les relations en une seule requête SQL.

### 2. Affichage des listes dans les JSP
*   **Problème :** Les listes déroulantes (Catégories) apparaissaient vides ou provoquaient des erreurs serveur.
*   **Solution :** Correction de la syntaxe JSP. Abandon des scriptlets `<% %>` obsolètes au profit de la **JSTL** (`<c:forEach>`) et du langage **EL** (`${cat.label}`). Vérification de la visibilité publique des getters dans les entités.

### 3. Gestion des Transactions
*   **Problème :** Assurer la cohérence des données lors de la création d'une annonce (création + lien avec user + lien avec catégorie).
*   **Solution :** Centralisation de la gestion des transactions dans la couche **Service**. Utilisation d'un `ThreadLocal` dans `EntityManagerHelper` pour garantir que le même EntityManager est utilisé tout au long d'une requête.

### 4. Filtre de sécurité
*   **Problème :** Le pattern d'URL `/annonce-*` n'était pas pris en compte par le filtre Servlet.
*   **Solution :** Spécification explicite des URLs à protéger dans l'annotation `@WebFilter` (`/annonce-list`, `/annonce-add`, etc.) car le joker `*` partiel n'est pas standard.
