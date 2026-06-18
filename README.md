# 📱 TODO — Système de Vie Personnel Premium

Todo est une application mobile Android moderne, élégante et ultra-fluide conçue avec **Jetpack Compose** et **Material Design 3**. Elle fait office de tableau de bord personnel premium pour suivre la productivité, gérer le temps via des séances de concentration (Focus) accompagnées d'ambiances sonores immersives synthétisées, et planifier vos journées.

---

## 🚀 Compilation Automatique sur GitHub (CI/CD)

Pour vous faciliter la vie, nous avons configuré **GitHub Actions**. Dès que vous poussez (push) ce code ou importez le projet sur votre GitHub :

1. Votre dépôt GitHub lancera automatiquement un compilateur en arrière-plan.
2. Pour récupérer l'application compilée :
   - Allez sur l'onglet **Actions** de votre dépôt GitHub.
   - Cliquez sur le dernier processus de build terminé (avec une icône verte ✅).
   - En bas de la page, dans la section **Artifacts**, cliquez sur **Todo-Debug-APK** pour télécharger le fichier d'installation `.apk`.
3. Transférez ce fichier `.apk` sur votre téléphone Android et ouvrez-le pour installer l'application immédiatement.

---

## 🛠️ Compilation Manuelle (Sur votre Ordinateur)

Si vous souhaitez modifier le code ou exécuter l'application localement sur votre ordinateur avec un émulateur ou votre appareil connecté :

### Prérequis
- **JDK 17** installé sur votre machine.
- **Android Studio** (version Koala ou plus récente recommandée) ou l'outil Gradle.

### Commandes utiles

Le projet contient à présent le **Gradle Wrapper** officiel (`gradlew`) pour vous permettre de compiler instantanément sans installer Gradle manuellement :

* **Pour compiler l'application en version Debug (crée un fichier APK installable) :**
  ```bash
  ./gradlew assembleDebug
  ```
  Le fichier `.apk` généré sera disponible à cet emplacement :
  `app/build/outputs/apk/debug/app-debug.apk`

* **Pour nettoyer les fichiers temporaires de build :**
  ```bash
  ./gradlew clean
  ```

---

## 🎨 Fonctionnalités & Architecture Mobile

- **Thème Visuel Fluo/Sombre** : Interface premium basée sur des nuances sombres de gris/bleu cosmique, avec des accents de dégradés cyan à violet.
- **Synthétiseur Audio Local** : Générateur de vagues de sons d'ambiance (silence, bruit blanc, pluie cosmique) gérant proprement le focus audio Android (`AudioManager`) pour éviter tout bug système.
- **Persistance Données Locale** : Vos données sont conservées localement et de manière sécurisée via une base de données **Room**.
- **Performance de Build Optimisée** : Toutes les polices, icônes adaptatives (icône Todo sur fond dégradé) et styles sont empaquetés de manière native pour s'exécuter à 60/120 images par seconde sur n'importe quel appareil mobile Android (SDK 24+).
