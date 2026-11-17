# Architecture avant migration

## Schema

```mermaid
flowchart TB
    U[Client HTTP]

    subgraph P["Projet Maven unique - un seul pom.xml"]
        E[Eureka Server]
        C[Config Server]
        G[API Gateway]
        CAT[Catalogue]
        CMD[Commande]
        PAY[Paiement]
    end

    DB[(PostgreSQL - base ecommerce commune)]

    U --> G
    G --> CAT
    G --> CMD
    G --> PAY

    CMD -->|OpenFeign| CAT
    PAY -->|OpenFeign| CMD

    CAT --> DB
    CMD --> DB
    PAY --> DB

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

L'architecture initiale regroupait six applications Spring Boot dans un seul projet Maven.
Chaque application disposait de sa propre classe principale et de son profil Spring.
Les trois services metier utilisaient une base PostgreSQL commune.
Eureka assurait la decouverte des services et Config Server centralisait la configuration.
Les appels entre services utilisaient OpenFeign.

## Limites

- Un seul projet Maven pour les six applications.
- Une base de donnees partagee entre les trois services metier.
- Une autonomie limitee pour la compilation et la gestion des dependances.
