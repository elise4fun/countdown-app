# Comptes à rebours - widget Android

Appli Android avec un widget d'écran d'accueil. Chaque widget affiche un compte à rebours (emoji, nom, temps restant, date). Tu peux en placer autant que tu veux.

## Compiler l'APK avec GitHub (sans rien installer)

1. Crée un dépôt vide sur github.com (bouton "New repository", nom au choix, public ou privé).
2. Envoie-y tout le contenu de ce dossier, **y compris le dossier caché `.github`**. Le plus fiable est d'utiliser git :
   ```
   cd countdown-app
   git init -b main
   git add -A
   git commit -m "Premiere version"
   git remote add origin https://github.com/TON_COMPTE/TON_DEPOT.git
   git push -u origin main
   ```
3. Dans le dépôt, onglet **Actions** : le workflow "Build APK" démarre tout seul et dure 3 à 6 minutes.
4. Quand il est vert, ouvre la page **Releases** du dépôt (colonne de droite) : le fichier `Countdown.apk` s'y trouve.

## Installer sur le téléphone

1. Depuis le navigateur du téléphone, ouvre la page Releases de ton dépôt (si le dépôt est privé, connecte-toi à GitHub) et télécharge `Countdown.apk`.
2. Ouvre le fichier. Android demande d'autoriser l'installation depuis le navigateur : accepte (Paramètres, Applis inconnues).
3. Si Play Protect affiche un avertissement ("appli non reconnue"), choisis "Installer quand même". C'est normal pour une appli compilée par toi.

## Utilisation

1. Ouvre l'appli "Comptes à rebours", touche **+ Ajouter**, choisis un emoji, un nom, une date et une heure.
2. Sur l'écran d'accueil : appui long sur un espace vide, **Widgets**, **Comptes à rebours**, glisse le widget où tu veux. Choisis le compte à rebours à afficher.
3. Répète pour afficher plusieurs comptes à rebours. Le widget est redimensionnable.
4. Touche un widget pour ouvrir l'appli et modifier ou supprimer un compte à rebours.

Le widget se met à jour environ toutes les 15 minutes (limite imposée par Android pour économiser la batterie).

## Compiler avec Android Studio (alternative)

Ouvre ce dossier dans Android Studio, laisse Gradle se synchroniser, puis menu Build, Build APK(s). Le fichier est dans `app/build/outputs/apk/debug/`.
