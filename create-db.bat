@echo off
echo Creation de la base de donnees PostgreSQL...
echo.
echo Entrez le mot de passe PostgreSQL quand demande
echo.

REM Ajout du chemin PostgreSQL
set PATH=%PATH%;C:\Program Files\PostgreSQL\16\bin;C:\Program Files\PostgreSQL\15\bin;C:\Program Files\PostgreSQL\14\bin

psql -U postgres -c "CREATE DATABASE messagerie_association;"
if %errorlevel% == 0 (
    echo.
    echo Base de donnees creee avec succes!
) else (
    echo.
    echo Erreur lors de la creation de la base de donnees
)
pause
