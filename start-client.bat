@echo off
echo Demarrage du client...
set "MAVEN_HOME=C:\Users\hp\Downloads\apache-maven-3.9.12-bin\apache-maven-3.9.12"
set "PATH=%MAVEN_HOME%\bin;%PATH%"
call mvn -DskipTests javafx:run
pause
