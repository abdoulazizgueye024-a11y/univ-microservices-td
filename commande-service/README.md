# Commande Service - Gestion des commandes

## 1. Présentation

Commande Service est le microservice chargé de gérer
les commandes de l'application e-commerce.

Il permet de créer, consulter, modifier le statut
et supprimer des commandes.

Chaque commande contient une ou plusieurs lignes
associées à des produits du Catalogue Service.

Le service possède sa propre base PostgreSQL :
commande_db.

## 2. Technologies

- Java 27
- Spring Boot 4.1.2
- Spring Cloud 2025.1.2
- Spring Web
- Spring Data JPA
- PostgreSQL 18
- Spring Cloud Config Client
- Eureka Client
- Spring Cloud OpenFeign
- Spring Boot Actuator
- Maven et Docker

Des dépendances Jersey sont également présentes
pour le transport du client Eureka.

## 3. Architecture interne

Classe principale :

src/main/java/com/ecommerce/launchers/CommandeApplication.java

Principaux composants :

- controller/CommandeController.java : API REST
- service/CommandeService.java : logique métier
- repository/CommandeRepository.java : accès PostgreSQL
- domain/Commande.java : entité commande
- domain/LigneCommande.java : entité ligne
- client/CatalogueClient.java : communication OpenFeign
- dto/ : objets de requête et de réponse
- exception/ : gestion des erreurs

L'application active OpenFeign avec @EnableFeignClients.

## 4. Modèle de données

Table commandes :

- id : identifiant généré
- dateCommande : date et heure de création
- statut : statut de la commande
- montantTotal : montant calculé
- lignes : liste des lignes associées

Table lignes_commande :

- id : identifiant généré
- produitId : identifiant du produit Catalogue
- produitNom : nom du produit
- quantite : quantité commandée
- prixUnitaire : prix récupéré dans Catalogue
- commande : référence à la commande parente

Une commande possède plusieurs lignes.

La relation JPA utilise CascadeType.ALL et
orphanRemoval=true.

Les lignes sont enregistrées avec la commande.

## 5. Statuts disponibles

Les valeurs définies dans StatutCommande sont :

- EN_ATTENTE
- CONFIRMEE
- EXPEDIEE
- LIVREE
- ANNULEE

Une nouvelle commande reçoit automatiquement
le statut EN_ATTENTE.

Le code ne définit pas de règles de transition
entre les différents statuts.

## 6. Endpoints REST

Préfixe :

    /api/v1/commandes

| Méthode | Endpoint | Description |
|---|---|---|
| GET | /api/v1/commandes | Lister les commandes |
| GET | /api/v1/commandes/{id} | Consulter une commande |
| POST | /api/v1/commandes | Créer une commande |
| PATCH | /api/v1/commandes/{id}/statut?statut=... | Modifier son statut |
| DELETE | /api/v1/commandes/{id} | Supprimer une commande |

Réponses prévues par le contrôleur :

- GET : HTTP 200
- POST : HTTP 201
- PATCH : HTTP 200
- DELETE : HTTP 204
- Commande inexistante : HTTP 404

PATCH et DELETE ont été testés avec succès
sur une commande temporaire via l'API Gateway.

## 7. Création d'une commande

Exemple JSON :

    {
      "lignes": [
        {
          "produitId": 1,
          "quantite": 2
        }
      ]
    }

URL via Gateway :

    http://localhost:9012/api/v1/commandes

Exemple PowerShell :

    $commande = @{
        lignes = @(
            @{
                produitId = 1
                quantite = 2
            }
        )
    } | ConvertTo-Json -Depth 5

    Invoke-RestMethod `
        -Uri "http://localhost:9012/api/v1/commandes" `
        -Method Post `
        -ContentType "application/json" `
        -Body $commande

Cette commande crée une donnée persistante.

Le produit demandé doit exister dans Catalogue.

## 8. Calcul du montant total

Lors de la création :

1. Le service reçoit les lignes de commande.
2. Pour chaque ligne, il appelle Catalogue via Feign.
3. Il récupère le nom et le prix du produit.
4. Il mémorise ces informations dans la ligne.
5. Il calcule le montant de chaque ligne.
6. Il additionne les montants.
7. Il enregistre la commande et ses lignes.

Formule :

    montantTotal = somme(prixUnitaire * quantite)

Exemple du scénario testé :

    Prix unitaire : 899.99
    Quantité : 2
    Montant total : 1799.98

Le prix et le montant utilisent le type Java Double.

## 9. Communication OpenFeign et Eureka

Le client CatalogueClient déclare :

    @FeignClient(name = "catalogue-service")

Il appelle :

    GET /api/v1/produits/{id}

Le nom logique catalogue-service permet
de découvrir le service dans Eureka.

Commande Service récupère les informations
du produit sans accéder directement
à la base catalogue_db.

Si Catalogue renvoie une erreur HTTP, le
gestionnaire FeignException du service Commande
la transforme en réponse HTTP.

Le scénario avec un produit inexistant a
été testé : réponse HTTP 404 et absence
de nouvelle commande enregistrée.

## 10. Configuration et base PostgreSQL

Le fichier application.properties définit :

    spring.application.name=commande-service
    server.port=8082
    spring.jpa.hibernate.ddl-auto=update

La configuration centralisée définit notamment
l'adresse Eureka avec EUREKA_SERVER_URL.

Le service importe la configuration distante
depuis CONFIG_SERVER_URL.

Dans Docker Compose :

- Serveur PostgreSQL : commande-db
- Port interne : 5432
- Base : commande_db
- Mot de passe : COMMANDE_DB_PASSWORD

Le mot de passe est fourni par le fichier
.env de la racine du projet.

Aucun secret ne doit être publié dans Git.

## 11. Compilation et déploiement

Depuis la racine du projet :

    .\mvnw.cmd -f commande-service/pom.xml clean package

JAR attendu :

    commande-service/target/commande-service-0.0.1-SNAPSHOT.jar

Construire l'image Docker :

    docker build -t commande-service:1.1 ./commande-service

Démarrer l'ensemble :

    docker compose up -d

Ports :

- Service dans Docker : 8082
- Service sur la machine hôte : 8095
- Gateway sur la machine hôte : 9012

Vérifier le conteneur :

    docker compose ps commande-service

Afficher les journaux :

    docker compose logs commande-service

## 12. Tests réalisés

Les vérifications déjà effectuées comprennent :

- Démarrage du conteneur Commande.
- Enregistrement dans Eureka avec le statut UP.
- Accès à la configuration centralisée.
- GET de la liste des commandes via Gateway.
- POST d'une commande avec réponse HTTP 201.
- Consultation d'une commande avec HTTP 200.
- Appel Feign vers Catalogue.
- Calcul du montant total 1799.98.
- Persistance de la commande dans commande_db.
- Persistance de sa ligne dans lignes_commande.
- GET d'une commande inexistante avec HTTP 404.
- POST avec un produit inexistant : HTTP 404.
- Vérification de l'absence de création en erreur.

Les preuves sont conservées dans preuves-td.

Les opérations PATCH et DELETE ont été validées :
6 tests réussis sur 6, avec vérification de la
commande initiale après suppression.

## 13. Limites et points d'attention

Le service ne vérifie pas explicitement que
la quantité demandée est positive.

Il ne vérifie pas explicitement la disponibilité
du stock et ne décrémente pas automatiquement
le stock du Catalogue Service.

Il ne définit pas de règles de transition
entre les statuts.

La conversion d'un statut repose sur
StatutCommande.valueOf(statut).

Une valeur inconnue peut donc provoquer une
exception et une réponse HTTP 500.

Les DTO examinés ne déclarent pas de contraintes
de validation métier explicites.

Une liste de lignes absente peut provoquer une
erreur lors de l'appel à dto.getLignes().size().

L'utilisation de Double pour les montants
n'est pas idéale pour les calculs monétaires.

Le service n'implémente pas de transaction
distribuée entre les microservices.

Le projet est réalisé dans un cadre pédagogique.