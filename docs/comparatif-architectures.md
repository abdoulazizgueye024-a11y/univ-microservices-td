# Comparaison des architectures avant et apres migration

| Critere | Avant migration | Apres migration |
|---|---|---|
| Organisation | Six applications Spring Boot reunies dans un seul projet Maven. | Six modules Maven independants, chacun avec son propre pom.xml. |
| Couplage | Dependances Maven partagees et base PostgreSQL commune aux services metier. | Dependances propres a chaque service et bases de donnees separees. |
| Maintenance | Une modification des dependances communes peut affecter plusieurs applications. | Chaque service peut etre modifie et compile independamment. |
| Scalabilite | L'organisation initiale complique la gestion independante des versions et des dependances. | Chaque service peut etre deploye ou multiplie selon ses besoins, sous reserve des ressources disponibles. |
| Deploiement | Applications lancees avec des profils distincts depuis le meme projet Maven. | Chaque service possede son Dockerfile et son image Docker. Docker Compose orchestre les conteneurs. |
| Donnees | Une base PostgreSQL ecommerce partagee. | Trois bases PostgreSQL dediees : catalogue_db, commande_db et paiement_db. |
| Communication | Gateway, Eureka, Config Server et OpenFeign deja presents. | Ces composants sont conserves dans des applications autonomes et conteneurisees. |
| Persistance | Donnees centralisees dans la base commune. | Trois volumes Docker persistants, dont la conservation des donnees a ete testee. |

## Bilan

La migration conserve les principales fonctionnalites du projet initial tout en separant les applications Spring Boot et leurs dependances.

Les services metier disposent maintenant de bases PostgreSQL distinctes. Les communications necessaires entre services passent par leurs API, notamment avec OpenFeign.

Cette organisation facilite la maintenance et le redeploiement independant des services. En contrepartie, elle augmente la complexite de configuration, de communication et de supervision.

Docker Compose permet de lancer et de verifier l'ensemble de l'architecture dans un environnement local.
