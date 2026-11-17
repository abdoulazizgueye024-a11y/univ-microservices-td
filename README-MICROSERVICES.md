# Mini-projet : architecture e-commerce en microservices

## 1. Présentation

Ce projet présente une architecture e-commerce développée avec
Spring Boot, Spring Cloud, PostgreSQL et Docker.

Il repose sur six applications Spring Boot indépendantes,
possédant chacune leur propre projet Maven et leur Dockerfile.

Le projet initial du professeur, décrit dans README.md,
est conservé séparément.

## 2. Architecture

Les six microservices sont :

1. discovery-service : serveur Eureka pour la découverte des services.
2. config-service : serveur Spring Cloud Config pour la configuration centralisée.
3. catalogue-service : gestion des produits.
4. commande-service : gestion des commandes.
5. paiement-service : gestion des paiements simulés.
6. gateway-service : point d'entrée HTTP unique.

Les communications entre services utilisent :

- Eureka pour la découverte.
- Spring Cloud OpenFeign pour les appels métier.
- Spring Cloud Gateway pour le routage.
- Spring Cloud Config pour la configuration centralisée.

Flux métier principal :

Client
  |
  v
Gateway
  |
  +--> Catalogue --> catalogue_db
  |
  +--> Commande  --> commande_db
  |       |
  |       +--> Catalogue (OpenFeign)
  |
  +--> Paiement  --> paiement_db
          |
          +--> Commande (OpenFeign)

Les trois services métier disposent chacun de leur propre
base de données PostgreSQL.

## 3. Technologies

- Java 27
- Spring Boot 4.1.2
- Spring Cloud 2025.1.2
- Maven
- PostgreSQL 18
- Docker et Docker Compose
- Spring Data JPA
- Spring Cloud Netflix Eureka
- Spring Cloud Config
- Spring Cloud OpenFeign
- Spring Cloud Gateway

## 4. Prérequis

Pour construire et lancer le projet :

- JDK 27
- Maven Wrapper fourni à la racine
- Docker Desktop avec Docker Compose
- Ports hôtes disponibles : 8764, 8891, 8094, 8095, 8096 et 9012

Les commandes suivantes sont destinées à PowerShell sous Windows.

## 5. Configuration des mots de passe

Copier le fichier d'exemple :

    Copy-Item .env.example .env

Modifier ensuite les trois mots de passe dans .env :

- CATALOGUE_DB_PASSWORD
- COMMANDE_DB_PASSWORD
- PAIEMENT_DB_PASSWORD

Ne jamais publier le véritable fichier .env.

Le fichier .gitignore exclut les identifiants locaux.

## 6. Compilation des six microservices

Depuis la racine du projet :

    $services = @(
        "discovery-service",
        "config-service",
        "catalogue-service",
        "commande-service",
        "paiement-service",
        "gateway-service"
    )

    foreach ($service in $services) {
        .\mvnw.cmd -f "$service/pom.xml" clean package
        if ($LASTEXITCODE -ne 0) {
            throw "Echec Maven : $service"
        }
    }

Les Dockerfiles attendent les fichiers JAR générés
dans les dossiers target des six projets.

## 7. Construction des images Docker

Depuis la racine du projet :

    docker build -t discovery-service:1.0 ./discovery-service
    docker build -t config-service:1.1 ./config-service
    docker build -t catalogue-service:1.2 ./catalogue-service
    docker build -t commande-service:1.1 ./commande-service
    docker build -t paiement-service:1.1 ./paiement-service
    docker build -t gateway-service:1.1 ./gateway-service

Les images ci-dessus sont locales.

La publication sur Docker Hub est une opération distincte.

## 8. Déploiement avec Docker Compose

Vérifier la configuration :

    docker compose config --quiet

Démarrer les services :

    docker compose up -d

Consulter les conteneurs :

    docker compose ps

Consulter les journaux :

    docker compose logs --tail 100

Arrêter les conteneurs :

    docker compose down

Attention : ne pas utiliser l'option -v si les données
des bases PostgreSQL doivent être conservées.

## 9. Services et ports

| Service | Port interne | Port hôte |
|---|---:|---:|
| Eureka | 8761 | 8764 |
| Config Server | 8888 | 8891 |
| Catalogue | 8081 | 8094 |
| Commande | 8082 | 8095 |
| Paiement | 8083 | 8096 |
| Gateway | 9000 | 9012 |

Eureka :

http://localhost:8764

Config Server :

http://localhost:8891/catalogue-service/default

Gateway :

http://localhost:9012

## 10. API métier via Gateway

Catalogue :

- GET /api/v1/produits
- GET /api/v1/produits/{id}
- POST /api/v1/produits

Commandes :

- GET /api/v1/commandes
- GET /api/v1/commandes/{id}
- POST /api/v1/commandes

Paiements :

- GET /api/v1/paiements
- GET /api/v1/paiements/{id}
- GET /api/v1/paiements/commande/{commandeId}
- POST /api/v1/paiements

Exemple de vérification PowerShell :

    Invoke-RestMethod http://localhost:9012/api/v1/produits

Les requêtes doivent passer par la Gateway pour
tester le routage et la découverte des services.

## 11. Bases de données et persistance

Trois conteneurs PostgreSQL 18 sont utilisés :

- catalogue-db : base catalogue_db
- commande-db : base commande_db
- paiement-db : base paiement_db

Chaque base possède son propre utilisateur et son propre
volume Docker nommé :

- catalogue-data
- commande-data
- paiement-data

Les volumes sont montés sur /var/lib/postgresql.

L'isolation des données entre les trois bases a été
vérifiée lors des tests du projet.

La persistance après recréation complète des conteneurs
reste à vérifier séparément.

## 12. Vérifications réalisées

Les vérifications effectuées comprennent :

- Validation du fichier Docker Compose.
- Démarrage des neuf conteneurs.
- État healthy des neuf conteneurs (six services et trois bases PostgreSQL).
- Enregistrement des quatre clients dans Eureka.
- Récupération des configurations via Config Server.
- Routage des requêtes HTTP par Gateway.
- Création et consultation d'un produit.
- Création d'une commande utilisant OpenFeign.
- Création d'un paiement utilisant OpenFeign.
- Vérification des données dans les trois bases.
- Vérification des réponses HTTP 404.
- Vérification des volumes PostgreSQL.

Les résultats et captures sont conservés dans
le dossier preuves-td.

## 13. Limites de l'implémentation

Le paiement est simulé : aucune transaction bancaire
réelle n'est effectuée.

La création d'un paiement accepté ne modifie pas
automatiquement le statut de la commande.

La création d'une commande ne garantit pas une
décrémentation automatique du stock.

57 tests automatisés ont réussi sur les services métier.
Ils ne couvrent pas tous les scénarios possibles.

Cette configuration est destinée au mini-projet
pédagogique et non à un déploiement de production.

## 14. Publication et rendu

Le rendu final doit comprendre :

- Le dépôt GitHub avec les six microservices.
- La documentation globale et individuelle.
- Les Dockerfiles et le fichier compose.yaml.
- Le fichier .env.example.
- Le compte rendu des opérations de build, tests,
  construction Docker et déploiement.
- Le lien du dépôt GitHub dans le compte rendu.
- Le lien de l'espace Docker Hub dans le compte rendu.

Dépôt GitHub : https://github.com/abdoulazizgueye024-a11y/univ-microservices-td
Docker Hub : https://hub.docker.com/u/abdoulaziz97

Date limite communiquée : avant le 15 décembre 2026.