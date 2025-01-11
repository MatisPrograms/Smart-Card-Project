# Smart-Card-Project

<p align="center">
  <img src="https://www.wanderglobe.org/wp-content/uploads/2021/04/Smart-Cards52-1536x816.jpg" alt="Java Smart Card">
</p>

## Description

Ce projet est une application Java pour la gestion des cartes à puce. Il utilise Maven pour la gestion des dépendances
et Ant pour les tâches de build. L'application permet de gérer les PINs, envoyer et recevoir des messages, et effectuer
des opérations cryptographiques comme la signature et le chiffrement des messages.

Ce projet est composé de trois parties :

- Une [**Applet Java Card**](java-card/README.md) pour la gestion des cartes à puce.
- Un [**Serveur de Vérification**](vending-machine/README.md) pour vérifier les signatures des messages.
- Un [**Distributeur Automatique**](verification-server/README.md) pour que l'utilisateur puisse passer des commandes.

## Prérequis

- Java 8 pour complier l'applet Java Card
- Java 23 ou supérieur pour exécuter les deux applications
- Maven 3.6 ou supérieur pour compiler le projet et gérer les dépendances
- Ant 1.10 ou supérieur pour exécuter les scripts de lancement
- D'autres encore que vous pouvez trouver dans les `README` des sous-projets

## Cloner le dépôt

Pour cloner le dépôt, exécutez la commande suivante :

```bash
  git clone https://github.com/MatisPrograms/Smart-Card-Project.git
  cd Smart-Card-Project
```

## Installation des dépendances

## Structure du projet

- `src/main/java`: Contient le code source de l'application.
- `pom.xml`: Fichier de configuration Maven.
- `build.xml`: Fichier de configuration Ant.