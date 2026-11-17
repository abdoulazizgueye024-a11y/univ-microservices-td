# Architecture apres migration

## Schema

```mermaid
flowchart TB
    U[Client HTTP]

    subgraph S["Six microservices - modules Maven independants"]
        E[Discovery Service - Eureka]
        C[Config Service]
        G[API Gateway]
        CAT[Catalogue Service]
        CMD[Commande Service]
        PAY[Paiement Service]
    end

    subgraph D["Bases PostgreSQL independantes"]
        DB1[(catalogue_db)]
        DB2[(commande_db)]
        DB3[(paiement_db)]
    end

    subgraph V["Volumes Docker persistants"]
        V1[(catalogue-data)]
        V2[(commande-data)]
        V3[(paiement-data)]
    end

    U -->|HTTP REST| G
    G -->|API produits| CAT
    G -->|API commandes| CMD
    G -->|API paiements| PAY

    CMD -->|OpenFeign| CAT
    PAY -->|OpenFeign| CMD

    CAT --> DB1
    CMD --> DB2
    PAY --> DB3

    DB1 --- V1
    DB2 --- V2
    DB3 --- V3

    G -.-> E
    CAT -.-> E
    CMD -.-> E
    PAY -.-> E

    C -.-> G
    C -.-> CAT
    C -.-> CMD
    C -.-> PAY
```

## Description

Le projet est organise en six modules Maven independants.
Chaque microservice possede son propre pom.xml et son Dockerfile.

La Gateway constitue le point entree des requetes HTTP.
Eureka permet la decouverte des services.
Config Server fournit la configuration centralisee.

Le service Commande consulte Catalogue avec OpenFeign.
Le service Paiement consulte Commande avec OpenFeign.

Chaque service metier possede sa propre base PostgreSQL.
Les donnees sont conservees dans trois volumes Docker distincts.

Docker Compose permet de demarrer les neuf conteneurs.
Des healthchecks permettent de verifier leur etat.

## Ameliorations par rapport a architecture initiale

- Six modules Maven autonomes au lieu du projet Maven unique.
- Une base PostgreSQL par service metier.
- Des images Docker propres a chaque microservice.
- Un deploiement commun avec Docker Compose.
- Des tests automatises et des controles de sante.
