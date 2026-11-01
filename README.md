# Univ — Eureka, Config Server, Gateway et microservices (un seul POM)

Ce projet utilise **un seul `pom.xml` et un seul code source Maven**, mais **six processus Spring Boot distincts** : Eureka Server, Config Server, API Gateway, catalogue, commandes et paiements. Chaque processus a sa propre classe `main` et son propre profil Spring.

## Prérequis

- JDK 27 (ou version compatible avec les dépendances), Maven Wrapper (`mvnw.cmd` sous Windows)
- PostgreSQL accessible sur `localhost:5433`, base `ecommerce`, utilisateur `postgres`, mot de passe `root` (ou variables `DB_URL`, `DB_USER`, `DB_PASSWORD`).
- Docker facultatif : `docker compose up -d` pour démarrer PostgreSQL si Docker est installé.

## Compiler

Dans le dossier contenant le **seul** `pom.xml` :

```powershell
.\mvnw.cmd clean package
```

## Démarrer les six applications

Ouvrir **six terminaux**, dans ce même dossier, et lancer dans l'ordre :

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.ecommerce.launchers.EurekaApplication' '-Dspring-boot.run.profiles=eureka'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.ecommerce.launchers.ConfigApplication' '-Dspring-boot.run.profiles=config,native'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.ecommerce.launchers.CatalogueApplication' '-Dspring-boot.run.profiles=catalogue'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.ecommerce.launchers.CommandeApplication' '-Dspring-boot.run.profiles=commande'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.ecommerce.launchers.PaiementApplication' '-Dspring-boot.run.profiles=paiement'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.ecommerce.launchers.GatewayApplication' '-Dspring-boot.run.profiles=gateway'
```

**Important :** une commande par terminal, pas les six dans le même terminal. Le Config Server utilise `native` et lit `./configurations/`, sans URI Git. Les services utilisent Eureka pour découvrir les noms `catalogue-service` et `commande-service` via OpenFeign. La Gateway utilise `lb://` pour router les requêtes.

## Ports

| Application | Port | URL |
|---|---:|---|
| Eureka Server | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888/application/default |
| Gateway | 9000 | http://localhost:9000 |
| Catalogue | 8081 | http://localhost:8081/swagger-ui.html |
| Commandes | 8082 | http://localhost:8082/swagger-ui.html |
| Paiements | 8083 | http://localhost:8083/swagger-ui.html |

Exemple : `GET http://localhost:9000/api/v1/produits` via Gateway. Les trois services partagent ici la base PostgreSQL `ecommerce` pour simplifier le développement ; une architecture de production devrait isoler leurs données.

## Diagnostic

- `Config Server ... Git URI` : vérifier que le profil `config` est bien activé ; démarrer avec les profils `config,native`.
- `No servers available for service` : attendre l'enregistrement des services sur Eureka (`http://localhost:8761`).
- Erreur de connexion PostgreSQL : vérifier Docker et les variables `DB_*`.
- Si le plugin Maven ne sélectionne pas la bonne classe, créer six configurations **Spring Boot** dans IntelliJ, chacune avec sa classe `com.ecommerce.launchers.*Application` et son profil (`eureka`, `config`, etc.).

**Validation :** cette archive a été préparée pour cette architecture ; le démarrage intégral des six processus n'a pas été vérifié dans cet environnement.
