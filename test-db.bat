@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
echo Test de connexion a la base de donnees...
mvnw.cmd exec:java -Dexec.mainClass="server.TestDB"
pause

