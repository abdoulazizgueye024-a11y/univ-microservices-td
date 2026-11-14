# Paiement Service - Gestion des paiements

## 1. Présentation

Paiement Service est le microservice chargé
de gérer les paiements associés aux commandes
de l'application e-commerce.

Il permet de :

- Consulter les paiements.
- Consulter un paiement par identifiant.
- Rechercher les paiements d'une commande.
- Créer un paiement simulé.
- Supprimer un paiement.

Le service possède sa propre base PostgreSQL :
paiement_db.

Il communique avec Commande Service par OpenFeign.

IMPORTANT :
Ce projet réalise une simulation de paiement.
Aucune transaction bancaire réelle n'est exécutée.

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
- Maven
- Docker et Docker Compose

Des dépendances Jersey sont présentes pour
le transport du client Eureka.

## 3. Architecture interne

Classe principale :

src/main/java/com/ecommerce/launchers/PaiementApplication.java

Principaux composants :

- controller/PaiementController.java : API REST
- service/PaiementService.java : logique métier
- repository/PaiementRepository.java : persistance
- domain/Paiement.java : entité JPA
- client/CommandeClient.java : client OpenFeign
- dto/CommandeDTO.java : données de commande
- dto/PaiementRequestDTO.java : requête
- dto/PaiementResponseDTO.java : réponse
- dto/PaiementMapper.java : conversion des données
- exception/ : gestion des erreurs

L'application active OpenFeign avec
@EnableFeignClients.

## 4. Modèle de données

L'entité Paiement correspond à la table :

    paiements

Champs :

- id : identifiant généré automatiquement
- commandeId : identifiant de la commande
- montant : montant du paiement
- modePaiement : mode choisi
- statut : statut du paiement
- datePaiement : date et heure de création

Le champ commandeId est un identifiant métier
du service distant.

Il ne correspond pas à une relation JPA
vers la base PostgreSQL de Commande Service.

Chaque microservice conserve ainsi
sa propre base de données.

## 5. Modes et statuts de paiement

Modes définis dans ModePaiement :

- CARTE_BANCAIRE
- VIREMENT
- PAYPAL
- ESPECES

Statuts définis dans StatutPaiement :

- EN_ATTENTE
- ACCEPTE
- REFUSE
- REMBOURSE

Lors de la création, le code affecte
systématiquement le statut ACCEPTE.

Les autres statuts sont définis dans l'entité,
mais aucune logique métier de refus,
de remboursement ou de transition de statut
n'est implémentée dans le service examiné.

## 6. Endpoints REST

Préfixe :

    /api/v1/paiements

| Méthode | Endpoint | Description |
|---|---|---|
| GET | /api/v1/paiements | Lister les paiements |
| GET | /api/v1/paiements/{id} | Consulter un paiement |
| GET | /api/v1/paiements/commande/{commandeId} | Paiements d'une commande |
| POST | /api/v1/paiements | Créer un paiement |
| DELETE | /api/v1/paiements/{id} | Supprimer un paiement |

Réponses prévues par le contrôleur :

- GET : HTTP 200
- POST : HTTP 201
- DELETE : HTTP 204
- Paiement inexistant par ID : HTTP 404

La recherche par commandeId renvoie une liste.
Si aucun paiement n'est trouvé, la liste
peut être vide avec HTTP 200.

L'opération DELETE a été validée sur un
paiement temporaire via l'API Gateway :
HTTP 204 après suppression, puis HTTP 404
lors de la consultation.

## 7. Création d'un paiement

Exemple de requête JSON :

    {
      "commandeId": 1,
      "modePaiement": "CARTE_BANCAIRE"
    }

URL via API Gateway :

    http://localhost:9012/api/v1/paiements

Exemple PowerShell :

    $paiement = @{
        commandeId = 1
        modePaiement = "CARTE_BANCAIRE"
    } | ConvertTo-Json

    Invoke-RestMethod `
        -Uri "http://localhost:9012/api/v1/paiements" `
        -Method Post `
        -ContentType "application/json" `
        -Body $paiement

ATTENTION :
Cet exemple crée un nouveau paiement persistant.

Il ne doit pas être exécuté plusieurs fois
sans tenir compte des données déjà présentes.

## 8. Logique métier

Lors de la création d'un paiement :

1. Le service reçoit commandeId et modePaiement.
2. Il appelle Commande Service via OpenFeign.
3. Il récupère la commande demandée.
4. Il lit son montantTotal.
5. Il crée une entité Paiement.
6. Il convertit le mode de paiement en enum.
7. Il affecte le statut ACCEPTE.
8. Il enregistre la date courante.
9. Il sauvegarde le paiement dans PostgreSQL.
10. Il retourne un PaiementResponseDTO.

Le montant n'est pas fourni par le client.
Il est récupéré directement auprès
du Commande Service.

Exemple du scénario métier testé :

    Commande ID : 1
    Montant commande : 1799.98
    Paiement ID : 1
    Montant paiement : 1799.98
    Mode : CARTE_BANCAIRE
    Statut paiement : ACCEPTE

Le paiement ne change pas automatiquement
le statut de la commande.

Dans le scénario testé, la commande initiale
conserve le statut EN_ATTENTE.

## 9. Communication OpenFeign et Eureka

Le client CommandeClient utilise :

    @FeignClient(name = "commande-service")

Il appelle :

    GET /api/v1/commandes/{id}

Eureka permet de localiser Commande Service
à partir de son nom logique.

Paiement Service ne se connecte pas
directement à commande_db.

Le DTO reçu contient notamment :

- id
- dateCommande
- statut
- montantTotal

Une commande inexistante entraîne une erreur
Feign traitée par le gestionnaire d'exceptions.

Le test déjà réalisé avec une commande
inexistante a retourné HTTP 404.

Aucun nouveau paiement n'a été enregistré
lors de ce test d'erreur.

## 10. Configuration et PostgreSQL

Le fichier application.properties définit :

    spring.application.name=paiement-service
    server.port=8083
    spring.jpa.hibernate.ddl-auto=update

Le service importe la configuration centralisée
depuis CONFIG_SERVER_URL.

La configuration distante fournit notamment
l'adresse Eureka avec EUREKA_SERVER_URL.

Dans Docker Compose :

- Service PostgreSQL : paiement-db
- Port PostgreSQL interne : 5432
- Base de données : paiement_db
- Variable du mot de passe : PAIEMENT_DB_PASSWORD

Les secrets sont conservés dans le fichier
.env à la racine du projet.

Le fichier .env ne doit pas être publié
sur GitHub.

## 11. Compilation et Docker

Depuis la racine du projet :

    .\mvnw.cmd -f paiement-service/pom.xml clean package

JAR attendu :

    paiement-service/target/paiement-service-0.0.1-SNAPSHOT.jar

Construction de l'image :

    docker build -t paiement-service:1.1 ./paiement-service

Démarrage de l'ensemble :

    docker compose up -d

Ports :

- Port interne du microservice : 8083
- Port exposé sur la machine : 8096
- API Gateway sur la machine : 9012

Vérifier le conteneur :

    docker compose ps paiement-service

Consulter les journaux :

    docker compose logs paiement-service

## 12. Tests réalisés

Les vérifications déjà effectuées comprennent :

- Démarrage du conteneur Paiement.
- Enregistrement dans Eureka avec statut UP.
- Récupération de la configuration centralisée.
- GET de la liste des paiements via Gateway.
- POST d'un paiement avec HTTP 201.
- Récupération de la commande via OpenFeign.
- Vérification du montant 1799.98.
- Vérification du statut ACCEPTE.
- Persistance du paiement dans paiement_db.
- GET du paiement ID 1 avec HTTP 200.
- GET des paiements de la commande ID 1.
- GET d'un paiement inexistant avec HTTP 404.
- POST avec commande inexistante : HTTP 404.
- Absence de nouveau paiement après cette erreur.

Les preuves sont conservées dans preuves-td.

Le test DELETE a été réalisé avec succès
sur le paiement temporaire ID 2.

Bilan : 7 tests réussis sur 7.

Le paiement temporaire a été supprimé.
Le paiement initial ID 1 et la commande
initiale ID 1 ont été préservés.

Preuve :
preuves-td/verification-delete-paiement.txt

## 13. Gestion des erreurs

Le service possède un gestionnaire global
d'exceptions.

Il traite notamment :

- ResourceNotFoundException : HTTP 404.
- MethodArgumentNotValidException : HTTP 400.
- FeignException : reprise du code HTTP distant
  lorsqu'il est disponible.
- Exception générale : HTTP 500.

Les DTO examinés ne possèdent pas
de contraintes de validation métier explicites.

Le contrôleur n'utilise pas @Valid sur
la requête de création.

La présence du gestionnaire HTTP 400
ne garantit donc pas une validation complète
des données fournies.

## 14. Limites et points d'attention

Le paiement est simulé.

Aucun fournisseur bancaire externe
n'est appelé.

Tous les nouveaux paiements valides
reçoivent automatiquement ACCEPTE.

Le code ne vérifie pas explicitement
si une commande a déjà été payée.

Plusieurs paiements peuvent donc être créés
pour une même commande.

Le code ne vérifie pas explicitement
le statut métier de la commande
avant d'accepter le paiement.

Le paiement ne modifie pas automatiquement
le statut de la commande.

Aucun remboursement réel n'est implémenté.

Le mode est converti avec
ModePaiement.valueOf(modePaiement).

Une valeur inconnue peut provoquer
une exception et une réponse HTTP 500.

Le montant utilise le type Java Double,
qui n'est pas idéal pour des calculs monétaires.

Aucune transaction distribuée n'est
implémentée entre Paiement et Commande.

Le projet est réalisé dans un cadre pédagogique.