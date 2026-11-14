# Discovery Service - Eureka Server

## 1. Présentation

Discovery Service est le serveur de découverte de services
de l'architecture e-commerce.

Il repose sur Netflix Eureka Server, intégré à Spring Cloud.

Son rôle est de maintenir un registre des instances
de microservices afin que les clients puissent les découvrir
par leur nom logique plutôt que par une adresse fixe.

Eureka ne remplace pas l'API Gateway : la Gateway assure
le routage HTTP des requêtes entrantes.

## 2. Technologies

- Java 27
- Spring Boot 4.1.2
- Spring Cloud 2025.1.2
- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator
- Maven
- Docker

## 3. Structure

Classe principale :

src/main/java/com/ecommerce/launchers/EurekaApplication.java

Configuration :

src/main/resources/application.properties

La classe EurekaApplication utilise :

- @SpringBootApplication
- @EnableEurekaServer

## 4. Configuration

Le fichier application.properties définit notamment :

    spring.application.name=discovery-service
    server.port=8761
    eureka.client.enabled=true
    eureka.client.register-with-eureka=false
    eureka.client.fetch-registry=false
    eureka.instance.hostname=localhost

Le serveur Eureka ne s'enregistre pas lui-même comme client.

La propriété hostname=localhost correspond à la configuration
locale. Les clients Docker utilisent l'adresse réseau
discovery-service:8761 grâce à Docker Compose.

## 5. Compilation

Depuis la racine du dépôt :

    .\mvnw.cmd -f discovery-service/pom.xml clean package

Le JAR attendu est :

    discovery-service/target/discovery-service-0.0.1-SNAPSHOT.jar

## 6. Construction de l'image Docker

Depuis la racine du dépôt, après compilation :

    docker build -t discovery-service:1.0 ./discovery-service

Le Dockerfile utilise eclipse-temurin:27-jre.

Il copie le JAR compilé et démarre l'application avec
java -jar.

## 7. Démarrage avec Docker Compose

Depuis la racine du dépôt :

    docker compose up -d

Le fichier compose.yaml publie le port interne 8761
sur le port 8764 de la machine hôte.

Tableau de correspondance :

| Élément | Adresse |
|---|---|
| Port interne | 8761 |
| Port hôte | 8764 |
| Interface Eureka depuis l'hôte | http://localhost:8764 |
| Adresse Eureka dans Docker | http://discovery-service:8761/eureka/ |

## 8. Vérifications

Consulter les conteneurs :

    docker compose ps

Consulter les journaux :

    docker compose logs discovery-service

Ouvrir l'interface Eureka :

    http://localhost:8764

Dans le scénario Docker Compose testé, quatre clients
étaient enregistrés avec le statut UP :

- catalogue-service
- commande-service
- paiement-service
- gateway-service

Le Config Server n'est pas inclus dans cette liste
de clients enregistrés lors de notre vérification.

Les preuves sont conservées dans le dossier
preuves-td à la racine du dépôt.

## 9. Rôle dans les communications

Commande Service utilise Eureka et OpenFeign pour
localiser Catalogue Service.

Paiement Service utilise Eureka et OpenFeign pour
localiser Commande Service.

Gateway utilise Eureka et Spring Cloud LoadBalancer
pour router les requêtes vers les services métier.

Ces mécanismes évitent de coder en dur les adresses
des instances dans les clients.

## 10. Limites

Le serveur Eureka est déployé ici comme une seule instance.

Cette configuration ne fournit pas de haute disponibilité
du registre de découverte.

Elle convient à la démonstration pédagogique du mini-projet.