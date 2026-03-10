@echo off
echo Demarrage du serveur...
set "MAVEN_HOME=C:\Users\hp\Downloads\apache-maven-3.9.12-bin\apache-maven-3.9.12"
set "PATH=%MAVEN_HOME%\bin;%PATH%"
rem L'ordre des arguments importe : options d'abord, puis goal exec:java
call mvn.cmd -DskipTests -Dexec.mainClass=server.MainServeur exec:java
pause
