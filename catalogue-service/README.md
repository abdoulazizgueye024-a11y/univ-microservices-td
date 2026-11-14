# Catalogue Service - Gestion des produits

## 1. Présentation

Catalogue Service est le microservice chargé de la gestion
des produits de l'application e-commerce.

Il expose une API REST permettant de créer, consulter,
modifier et supprimer des produits.

Il dispose de sa propre base PostgreSQL, catalogue_db.

Il est indépendant des bases de données des services
Commande et Paiement.

## 2. Technologies

- Java 27
- Spring Boot 4.1.2
- Spring Cloud 2025.1.2
- Spring Web
- Spring Data JPA
- PostgreSQL 18
- Spring Cloud Config Client
- Eureka Client
- Spring Boot Actuator
- Maven et Docker

Le projet inclut également des dépendances Jersey
pour le transport du client Eureka.

## 3. Organisation du code

Classe de démarrage :

src/main/java/com/ecommerce/launchers/CatalogueApplication.java

Principaux composants :

- controller/ProduitController.java : API REST
- domain/Produit.java : entité JPA
- dto/ProduitRequestDTO.java : données entrantes
- dto/ProduitResponseDTO.java : données retournées
- dto/ProduitMapper.java : conversion DTO/entité
- repository/ProduitRepository.java : accès aux données
- service/ProduitService.java : logique métier
- exception/GlobalExceptionHandler.java : gestion des erreurs
- exception/ResourceNotFoundException.java : produit absent

L'application utilise @EntityScan et
@EnableJpaRepositories pour détecter ses composants JPA.

## 4. Modèle de données

L'entité Produit est associée à la table produits.

| Champ | Type Java | Description |
|---|---|---|
| id | Long | Identifiant généré automatiquement |
| nom | String | Nom du produit |
| description | String | Description |
| prix | Double | Prix du produit |
| quantiteStock | Integer | Quantité en stock |

Dans l'entité JPA, nom, prix et quantiteStock
sont déclarés non nullables.

Le DTO de création contient :

- nom
- description
- prix
- quantiteStock

Le DTO de réponse contient également l'identifiant id.

Aucune validation métier explicite du prix ou du stock
n'a été identifiée dans le code examiné.

## 5. Endpoints REST

Préfixe de l'API :

    /api/v1/produits

| Méthode | Endpoint | Description |
|---|---|---|
| GET | /api/v1/produits | Lister les produits |
| GET | /api/v1/produits/{id} | Consulter un produit |
| POST | /api/v1/produits | Créer un produit |
| PUT | /api/v1/produits/{id} | Modifier un produit |
| DELETE | /api/v1/produits/{id} | Supprimer un produit |

Codes HTTP définis dans le contrôleur :

- GET : 200 en cas de succès
- POST : 201 en cas de création
- PUT : 200 en cas de modification
- DELETE : 204 en cas de suppression
- Ressource inexistante : 404

Les opérations PUT et DELETE ont été testées avec succès
via l'API Gateway.

## 6. Exemple de création d'un produit

URL via Gateway :

    http://localhost:9012/api/v1/produits

Exemple de corps JSON :

    {
      "nom": "Clavier USB",
      "description": "Clavier pour ordinateur",
      "prix": 29.99,
      "quantiteStock": 20
    }

Exemple PowerShell :

    $produit = @{
        nom = "Clavier USB"
        description = "Clavier pour ordinateur"
        prix = 29.99
        quantiteStock = 20
    } | ConvertTo-Json

    Invoke-RestMethod `
        -Uri "http://localhost:9012/api/v1/produits" `
        -Method Post `
        -ContentType "application/json" `
        -Body $produit

Cette commande crée une nouvelle donnée dans la base.

## 7. Consultation des produits

Lister les produits :

    Invoke-RestMethod http://localhost:9012/api/v1/produits

Consulter le produit d'identifiant 1 :

    Invoke-RestMethod http://localhost:9012/api/v1/produits/1

Le résultat dépend des données réellement présentes
dans la base catalogue_db.

## 8. Configuration

Le fichier application.properties définit notamment :

    spring.application.name=catalogue-service
    server.port=8081
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.open-in-view=false

Le service utilise Spring Cloud Config pour récupérer
une partie de sa configuration centralisée.

La configuration centralisée définit notamment
l'adresse du registre Eureka via EUREKA_SERVER_URL.

Dans Docker Compose, les paramètres de connexion sont :

- Serveur PostgreSQL : catalogue-db
- Port PostgreSQL : 5432
- Base : catalogue_db
- Utilisateur : catalogue_user
- Mot de passe : CATALOGUE_DB_PASSWORD

Le mot de passe est fourni par le fichier .env
à la racine du projet.

## 9. Compilation et image Docker

Depuis la racine du dépôt, compiler :

    .\mvnw.cmd -f catalogue-service/pom.xml clean package

JAR attendu :

    catalogue-service/target/catalogue-service-0.0.1-SNAPSHOT.jar

Construire ensuite l'image :

    docker build -t catalogue-service:1.2 ./catalogue-service

Le Dockerfile utilise eclipse-temurin:27-jre.

## 10. Déploiement Docker Compose

Depuis la racine du dépôt :

    docker compose up -d

Ports :

| Élément | Port |
|---|---:|
| Catalogue dans Docker | 8081 |
| Catalogue sur la machine hôte | 8094 |
| Gateway sur la machine hôte | 9012 |

Accès direct au service :

    http://localhost:8094/api/v1/produits

Accès recommandé pour tester le routage :

    http://localhost:9012/api/v1/produits

Vérifier le conteneur :

    docker compose ps catalogue-service

Consulter ses journaux :

    docker compose logs catalogue-service

## 11. Communication avec les autres services

Catalogue Service s'enregistre dans Eureka.

Commande Service peut retrouver Catalogue Service
par son nom logique et l'interroger avec OpenFeign.

Lors du scénario métier testé, Commande Service a
récupéré les informations du produit sélectionné.

Catalogue Service ne contacte pas directement
les bases PostgreSQL de Commande ou Paiement.

## 12. Tests et résultats

Les vérifications déjà réalisées comprennent :

- Démarrage du conteneur Catalogue.
- Enregistrement dans Eureka avec le statut UP.
- Consultation de produits via Gateway.
- Création d'un produit avec réponse HTTP 201.
- Consultation d'un produit par identifiant avec HTTP 200.
- Vérification de son enregistrement dans PostgreSQL.
- Recherche d'un produit inexistant avec HTTP 404.
- Utilisation du produit dans une commande via OpenFeign.

Les preuves sont conservées dans preuves-td.

Les tests PUT et DELETE ont réussi : HTTP 200 pour PUT,
HTTP 204 pour DELETE et HTTP 404 après suppression.

## 13. Limites

La gestion du stock est représentée par le champ
quantiteStock, mais aucune décrémentation automatique
du stock lors d'une commande n'a été établie.

Les DTO ne déclarent pas de contraintes de validation
explicites telles que @NotNull ou @Min.

Le prix utilise Double, ce qui n'est pas idéal pour
les calculs monétaires exigeant une précision décimale.

Le paramètre ddl-auto=update convient au contexte
pédagogique, mais ne remplace pas une stratégie
de migration de schéma pour la production.

Ce service est destiné au mini-projet pédagogique.