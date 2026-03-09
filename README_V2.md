# Messagerie Association - Version 2.1

## Prerequis
- Java 17+
- Maven
- MySQL 8+

## Base de donnees
1. Initialiser MySQL:
```bash
mysql -u root -p < init-db.sql
```
2. Verifier la configuration:
`src/main/resources/META-INF/persistence.xml`

MySQL est la configuration par defaut.
PostgreSQL reste possible via `init-db-postgresql.sql` + proprietes alternatives dans `persistence.xml`.

## Demarrage
1. Serveur:
- `start-server.bat`
- ou `mvnw.cmd exec:java -Dexec.mainClass="server.MainServeur"`

2. Client:
- `start-client.bat`
- ou `mvnw.cmd javafx:run`

## Structure du projet
```text
src/main/java/
  client/
    controller/
    model/
    network/
    AppContext.java
    MainClient.java
  server/
    handler/
      ClientHandler.java
    MainServeur.java
    TestDB.java
  model/
  dao/
  service/
  config/
  util/
```

## Technologies
- Java 17
- JavaFX 21
- Sockets Java
- JPA/Hibernate
- MySQL

## Documentation
- `README.md`
- `docs/ARCHITECTURE_PROFESSIONNELLE.md`
- `docs/EXPLICATION_FICHIERS.md`
- `docs/uml-class-diagram.puml`
