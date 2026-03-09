# Guide de demarrage rapide

## 1) Prerequis
- Java 17+
- Maven
- MySQL 8+

## 2) Initialiser la base
```bash
mysql -u root -p < init-db.sql
```

## 3) Configurer la connexion
Fichier: `src/main/resources/META-INF/persistence.xml`
- URL MySQL par defaut: `jdbc:mysql://localhost:3306/messagerie_association...`
- Ajuster `user` et `password` selon votre poste.

## 4) Lancer l'application
1. Lancer le serveur:
```bash
mvnw.cmd exec:java -Dexec.mainClass="server.MainServeur"
```
2. Lancer le client (nouveau terminal):
```bash
mvnw.cmd javafx:run
```

## 5) Scripts Windows
- `start-server.bat`
- `start-client.bat`

## 6) Flux utilisateur
1. Inscription
2. Connexion
3. Selection d'un membre
4. Envoi / reception des messages
5. Historique charge automatiquement

## 7) Arborescence cible implementee
```text
src/main/java/
  client/
  server/
    handler/
  model/
  dao/
  service/
  config/
  util/
```

## 8) Depannage
- `Connection refused`: demarrer le serveur d'abord.
- Erreur DB: verifier MySQL et `persistence.xml`.
- Build: `mvnw.cmd -DskipTests package`.

## 9) Option PostgreSQL
- Script: `init-db-postgresql.sql`
- Proprietes JDBC PostgreSQL deja presentes en commentaire dans `persistence.xml`.
