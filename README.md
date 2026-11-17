# Jeu des 10 000

Application Android native, utilisable hors ligne, pour suivre une partie de
10 000 à plusieurs joueurs. Les parties et les scores sont conservés dans la
base SQLite privée de l'application sur le téléphone.

## Fonctionnalités

- Reprendre la partie en cours ou commencer une partie de 2 à 8 joueurs.
- Consulter les parties récentes et leurs vainqueurs.
- Exiger 600 points en un tour pour qu'un joueur entre dans la partie.
- Enregistrer des points, une correction positive ou négative, ou un échec.
- Passer automatiquement au joueur suivant après un score ou un échec.
- Suivre les échecs par palier ; au troisième, retirer le gain de ce palier.
- En cas d'égalité, retirer en cascade le dernier gain des adversaires concernés.
- Voir les paliers les plus récents en premier, avec le total atteint.
- Toucher un joueur pour consulter son détail, même si ce n'est pas son tour.
- Consulter l'évolution des scores et les totaux après chaque saisie.
- Annuler une saisie (avec confirmation).
- Lire les règles en français, anglais ou espagnol.
- Consommation réduite : aucune publicité, connexion réseau, animation continue ni tâche en arrière-plan.

## Construire l'application

Prérequis : JDK 17 ou 21 et Android SDK avec la plateforme Android 35. Depuis la
racine du projet :

```sh
./gradlew assembleDebug
```

L'APK de test est généré dans `app/build/outputs/apk/debug/app-debug.apk`.
Une copie de l'APK debug est également conservée dans le dépôt :
`apk/jeu-des-10000-v1.1-debug.apk`. Les versions précédentes restent
archivées dans `apk/`. Il s'agit d'une version de test, pas d'un APK de
production signé.

Pour installer la copie conservée sur un appareil connecté avec ADB :

```sh
adb install -r apk/jeu-des-10000-v1.1-debug.apk
```

Le projet utilise le plugin Android Gradle et son wrapper Gradle ; Gradle peut
télécharger ces outils lors du premier build. Aucun service externe n'est
nécessaire à l'exécution de l'application.

## Règles

Les règles de référence de l'application, en français, anglais et espagnol,
sont conservées dans [REGLES.md](./REGLES.md). Les combinaisons et les valeurs
du jeu des 10 000 variant selon les tables, les joueurs peuvent convenir de
leurs variantes ; les points sont saisis manuellement.
