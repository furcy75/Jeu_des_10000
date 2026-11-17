# Jeu des 10 000

Application Android native, utilisable hors ligne, pour suivre une partie de
10 000 à plusieurs joueurs. Les parties et les scores sont conservés dans la
base SQLite privée de l'application sur le téléphone.

## Fonctionnalités

- Reprendre la partie en cours ou commencer une partie de 2 à 8 joueurs.
- Consulter les parties récentes et leurs vainqueurs.
- Enregistrer des points, une correction positive ou négative, ou un échec.
- Retirer le dernier gain après trois échecs consécutifs.
- En cas d'égalité, retirer le dernier gain de chaque adversaire ayant ce total.
- Consulter l'évolution des scores et annuler une saisie (avec confirmation).
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
`apk/jeu-des-10000-v1.0-debug.apk`. Il s'agit d'une version de test,
pas d'un APK de production signé.

Pour installer la copie conservée sur un appareil connecté avec ADB :

```sh
adb install -r apk/jeu-des-10000-v1.0-debug.apk
```

Le projet utilise le plugin Android Gradle et son wrapper Gradle ; Gradle peut
télécharger ces outils lors du premier build. Aucun service externe n'est
nécessaire à l'exécution de l'application.

## Règles

Les règles de référence de l'application, en français, anglais et espagnol,
sont conservées dans [REGLES.md](./REGLES.md). Les combinaisons et les valeurs
du jeu des 10 000 variant selon les tables, les joueurs peuvent convenir de
leurs variantes ; les points sont saisis manuellement.
