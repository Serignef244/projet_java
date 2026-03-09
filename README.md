# Messagerie Association - Guide Demarrage

## Prerequis
- Java 17+
- MySQL 8+
- Maven

## 1) Initialiser la base MySQL

```bash
mysql -u root -p < init-db.sql
```

## 2) Configurer la connexion DB

Fichier:
`src/main/resources/META-INF/persistence.xml`

Par defaut, MySQL est active:
- driver: `com.mysql.cj.jdbc.Driver`
- url: `jdbc:mysql://localhost:3306/messagerie_association?...`
- user: `root`
- password: a renseigner selon votre machine

## 3) Lancer l'application

1. Serveur:
```bash
./mvnw -DskipTests exec:java -Dexec.mainClass="server.MainServeur"
```

2. Client (dans un autre terminal):
```bash
./mvnw -DskipTests exec:java -Dexec.mainClass="client.MainClient"
```

Ou utilisez:
- `start-server.bat`
- `start-client.bat`

## Livrables de conception

- Architecture: `docs/ARCHITECTURE_PROFESSIONNELLE.md`
- UML (PlantUML): `docs/uml-class-diagram.puml`
- Explication des fichiers: `docs/EXPLICATION_FICHIERS.md`

## Note PostgreSQL (optionnelle)

Un script legacy est fourni: `init-db-postgresql.sql`
Et des proprietes PostgreSQL alternatives sont gardees en commentaire dans `persistence.xml`.

