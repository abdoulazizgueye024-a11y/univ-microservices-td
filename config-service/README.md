# Config Service - Spring Cloud Config Server

## 1. Présentation

Config Service centralise la configuration des applications
de l'architecture e-commerce en microservices.

Il utilise Spring Cloud Config Server.

Son objectif est de fournir aux services clients leurs
propriétés de configuration à partir d'un emplacement centralisé.

Dans ce mini-projet, les configurations sont intégrées
directement aux ressources du Config Server.

## 2. Technologies

- Java 27
- Spring Boot 4.1.2
- Spring Cloud 2025.1.2
- Spring Cloud Config Server
- Spring Boot Actuator
- Maven
- Docker

## 3. Structure

Classe principale :

src/main/java/com/ecommerce/launchers/ConfigApplication.java

Configuration principale :

src/main/resources/application.properties

Configurations centralisées :

src/main/resources/configurations/

La classe ConfigApplication utilise :

- @SpringBootApplication
- @EnableConfigServer

## 4. Configuration du serveur

Le fichier application.properties contient :

    spring.application.name=config-server
    server.port=8888
    spring.profiles.active=native
    spring.cloud.config.server.native.search-locations=classpath:/configurations/
    eureka.client.enabled=false

Le profil native indique que les configurations sont
chargées depuis un emplacement local au serveur.

Le préfixe classpath: permet de lire les ressources
intégrées au JAR Spring Boot.

Le Config Server n'est pas enregistré comme client Eureka
dans cette configuration.

## 5. Applications configurées

Quatre fichiers de configuration principaux sont présents :

- catalogue-service.properties
- commande-service.properties
- paiement-service.properties
- gateway-service.properties

Ces fichiers correspondent aux applications clientes
du Config Server.

Les fichiers de sauvegarde portant le suffixe .bak-*
ne font pas partie des quatre configurations principales.

## 6. Compilation

Depuis la racine du dépôt :

    .\mvnw.cmd -f config-service/pom.xml clean package

Le JAR attendu est :

    config-service/target/config-service-0.0.1-SNAPSHOT.jar

Les ressources de configuration sont intégrées au JAR
pendant la compilation Maven.

## 7. Construction de l'image Docker

Depuis la racine du dépôt, après compilation :

    docker build -t config-service:1.1 ./config-service

Le Dockerfile utilise :

    eclipse-temurin:27-jre

Il copie le JAR compilé et lance l'application
avec la commande java -jar.

## 8. Démarrage avec Docker Compose

Depuis la racine du dépôt :

    docker compose up -d

Le fichier compose.yaml définit notamment :

- Port interne : 8888
- Port hôte : 8891
- Profil actif : native
- Emplacement : classpath:/configurations/

Adresse depuis la machine hôte :

    http://localhost:8891

Adresse depuis les autres conteneurs :

    http://config-service:8888/

Les applications clientes utilisent cette adresse
interne pour contacter le Config Server.

## 9. Vérification des configurations

Depuis PowerShell, une fois les conteneurs démarrés :

    Invoke-RestMethod http://localhost:8891/catalogue-service/default

    Invoke-RestMethod http://localhost:8891/commande-service/default

    Invoke-RestMethod http://localhost:8891/paiement-service/default

    Invoke-RestMethod http://localhost:8891/gateway-service/default

Le chemin suit le format :

    /{application}/{profil}

Lors de nos tests, les quatre configurations ont été
récupérées avec succès.

La preuve correspondante est conservée dans :

    preuves-td/verification-config-server.txt

Une capture est également disponible :

    preuves-td/capture-config-server.png

## 10. Relation avec les autres composants

Le Config Server fournit les propriétés centralisées
aux quatre applications clientes.

Eureka assure la découverte des instances.

Gateway assure le routage des requêtes HTTP.

OpenFeign permet les communications métier entre
Commande et Catalogue, ainsi qu'entre Paiement et Commande.

Le Config Server ne remplace aucun de ces composants.

## 11. Limites

Les configurations sont stockées dans les ressources
du projet et intégrées au JAR.

Une modification de ces fichiers nécessite donc
une nouvelle construction du JAR et de l'image Docker
pour être intégrée à cette distribution.

Le mini-projet utilise une seule instance du Config Server.

Cette configuration est destinée à un environnement
pédagogique et non à une infrastructure de production.