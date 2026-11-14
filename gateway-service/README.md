# Gateway Service - API Gateway e-commerce

## 1. Présentation

Gateway Service constitue le point d'entrée HTTP
unique de l'architecture microservices e-commerce.

Il reçoit les requêtes des clients et les
transmet aux microservices concernés.

Il ne contient pas la logique métier
du Catalogue, des Commandes ou des Paiements.

Le routage est réalisé avec
Spring Cloud Gateway Server Web MVC.

## 2. Technologies

- Java 27
- Spring Boot 4.1.2
- Spring Cloud 2025.1.2
- Spring Cloud Gateway Server Web MVC
- Spring Web MVC
- Spring Cloud Netflix Eureka Client
- Spring Cloud LoadBalancer
- Spring Cloud Config Client
- Spring Boot Actuator
- Maven
- Docker
- Docker Compose

Des dépendances Jersey 3 sont utilisées
pour le transport du client Eureka.

Le Gateway utilise Spring MVC et non WebFlux.

## 3. Architecture

Le client envoie ses requêtes à :

    http://localhost:9012

Le Gateway écoute sur le port 9000
à l'intérieur du conteneur Docker.

Les requêtes sont orientées vers :

- catalogue-service
- commande-service
- paiement-service

Les services sont découverts grâce à Eureka.

Les destinations utilisent le préfixe lb://,
résolu avec Spring Cloud LoadBalancer.

Schéma logique :

    Client HTTP
         |
         v
    API Gateway
         |
         +----> Catalogue Service
         |
         +----> Commande Service
         |
         +----> Paiement Service

Eureka fournit les informations nécessaires
à la découverte des instances des services.

Le Gateway ne possède pas de base PostgreSQL.

## 4. Organisation du code

Classe principale :

    com.ecommerce.launchers.GatewayApplication

Cette classe démarre Spring Boot et configure
le scan du package :

    com.ecommerce.univ.gateway

Classe de configuration :

    com.ecommerce.univ.gateway.config.EurekaTransportConfig

Elle fournit une fabrique de transport
Jersey 3 pour le client Eureka.

Les routes HTTP sont déclarées
dans les fichiers application.properties
et gateway-service.properties.

Aucun contrôleur métier spécifique
n'est nécessaire pour ces trois routes.

## 5. Routes configurées

Préfixe commun des API :

    /api/v1

Route Catalogue :

    ID : catalogue
    URI : lb://catalogue-service
    Chemins :
      /api/v1/produits
      /api/v1/produits/**

Route Commande :

    ID : commande
    URI : lb://commande-service
    Chemins :
      /api/v1/commandes
      /api/v1/commandes/**

Route Paiement :

    ID : paiement
    URI : lb://paiement-service
    Chemins :
      /api/v1/paiements
      /api/v1/paiements/**

Les chemins sont transmis aux services
sans réécriture explicite.

Les routes ne sont pas générées
automatiquement pour tous les services Eureka :
elles sont déclarées explicitement.

## 6. Exemples d'appels

Catalogue :

    GET http://localhost:9012/api/v1/produits

    GET http://localhost:9012/api/v1/produits/1

Commande :

    GET http://localhost:9012/api/v1/commandes

    GET http://localhost:9012/api/v1/commandes/1

Paiement :

    GET http://localhost:9012/api/v1/paiements

    GET http://localhost:9012/api/v1/paiements/1

Paiements d'une commande :

    GET http://localhost:9012/api/v1/paiements/commande/1

Les méthodes POST, PUT, PATCH et DELETE
sont transmises au service cible lorsque
son contrôleur prend en charge ces méthodes.

Le Gateway ne crée pas lui-même
les produits, commandes ou paiements.

## 7. Découverte de services avec Eureka

La configuration locale active :

    eureka.client.enabled=true

    eureka.client.register-with-eureka=true

    eureka.client.fetch-registry=true

Le Gateway s'enregistre auprès d'Eureka
et récupère le registre des services.

Dans Docker Compose, l'adresse Eureka
est fournie par EUREKA_SERVER_URL.

L'adresse interne attendue est :

    http://discovery-service:8761/eureka/

Le préfixe lb:// permet de désigner
un service par son nom logique.

Par exemple :

    lb://catalogue-service

Cette destination est résolue
grâce à la découverte de services
et au composant LoadBalancer.

## 8. Configuration centralisée

Le Gateway est client de Spring Cloud Config.

Le fichier local importe :

    optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}

Dans Docker Compose :

    CONFIG_SERVER_URL=http://config-service:8888/

La configuration centralisée est conservée
dans Config Server :

    configurations/gateway-service.properties

Elle contient notamment :

- Le port 9000.
- Le nom gateway-service.
- L'adresse Eureka configurable.
- L'activation de Gateway Web MVC.
- Les trois routes HTTP.
- La configuration des endpoints Actuator.

Le préfixe optional permet un démarrage
sans imposer systématiquement la disponibilité
du Config Server au moment de l'import.

Les routes figurent à la fois dans
la configuration locale et centralisée.

## 9. Compilation et image Docker

Depuis la racine du projet :

    .\mvnw.cmd -f gateway-service/pom.xml clean package

JAR attendu :

    gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar

Le Dockerfile utilise :

    eclipse-temurin:27-jre

Il copie le JAR dans :

    /app/app.jar

Commande de démarrage :

    java -jar /app/app.jar

Construction de l'image :

    docker build -t gateway-service:1.1 ./gateway-service

Démarrage avec Docker Compose :

    docker compose up -d

Vérification :

    docker compose ps gateway-service

Journaux :

    docker compose logs gateway-service

## 10. Ports et réseau Docker

Port interne du Gateway :

    9000

Port exposé sur l'ordinateur :

    9012

Correspondance :

    localhost:9012 -> gateway-service:9000

Le Gateway communique avec les services
sur le réseau Docker Compose.

Les noms des services sont utilisés
à l'intérieur de ce réseau.

Les ports 8094, 8095 et 8096 sont les ports
hôtes des services métier dans le déploiement
de ce projet.

Le Gateway n'utilise pas ces ports hôtes
pour résoudre ses destinations lb://.

## 11. Tests réalisés

Les tests précédemment réalisés comprennent :

- Démarrage du conteneur Gateway.
- Enregistrement du Gateway dans Eureka.
- Récupération de sa configuration centralisée.
- GET de la liste des produits : HTTP 200.
- GET de la liste des commandes : HTTP 200.
- GET de la liste des paiements : HTTP 200.
- GET du produit ID 1 : HTTP 200.
- GET de la commande ID 1 : HTTP 200.
- GET du paiement ID 1 : HTTP 200.
- GET des paiements de la commande ID 1.
- Création d'un produit : HTTP 201.
- Création d'une commande : HTTP 201.
- Création d'un paiement : HTTP 201.
- Mise à jour et suppression d'un produit.
- Modification et suppression d'une commande.
- Suppression d'un paiement temporaire.
- Vérification de réponses HTTP 404
  pour des ressources inexistantes.

Le scénario métier a confirmé
la communication entre les services :

    Client -> Gateway -> Commande
                         |
                         v
                      Catalogue

    Client -> Gateway -> Paiement
                         |
                         v
                       Commande

Les preuves sont conservées
dans le dossier preuves-td.

Les tests valident les scénarios exécutés.
Ils ne constituent pas une campagne complète
de tests de charge ou de sécurité.

## 12. Supervision

Le projet utilise Spring Boot Actuator.

La configuration locale expose :

    health,info

La configuration centralisée définit :

    management.endpoints.web.exposure.include=*

Cette configuration étendue est adaptée
aux vérifications du projet pédagogique,
mais ne doit pas être reprise telle quelle
dans un environnement de production.

Une vérification de santé peut être réalisée
sur le port hôte du Gateway :

    http://localhost:9012/actuator/health

La présence de l'endpoint ne garantit pas
à elle seule que tous les services métier
sont opérationnels.

## 13. Limites et points d'attention

Le code examiné ne définit pas
d'authentification au niveau du Gateway.

Il ne configure pas de contrôle d'accès
par rôle, de limitation de débit
ni de politique CORS personnalisée.

Aucun mécanisme explicite de circuit breaker
ou de retry n'a été identifié
dans les configurations examinées.

Le déploiement local utilise HTTP.
Une exposition publique nécessiterait
une configuration HTTPS adaptée.

L'exposition de tous les endpoints Actuator
doit être restreinte en production.

Le Gateway ne possède pas de logique métier
et ne garantit pas la cohérence transactionnelle
entre les bases des microservices.

Les routes sont déclarées explicitement :
l'ajout d'un nouveau service métier
nécessite de configurer sa route.

Les tests de performance, de résilience
et de sécurité restent à réaliser.

Ce service est développé dans le cadre
d'un projet pédagogique de migration
d'une application monolithique
vers une architecture microservices.