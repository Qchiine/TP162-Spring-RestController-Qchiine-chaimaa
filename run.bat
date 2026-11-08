@echo off
title TP8-Spring-RestController
chcp 65001 >nul
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "MAVEN_OPTS=-Dfile.encoding=UTF-8"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

:menu
cls
echo ==============================================================================
echo   TP 8 - Spring @RestController : API bancaire
echo   Spring Boot 3.3.1  /  Spring Data JPA  /  H2 memoire
echo   Swagger (springdoc-openapi)  /  JSON + XML  /  JDK 17
echo ==============================================================================
echo.
echo   [1] Lancer l'application    (http://localhost:8082)
echo   [2] Suite de tests d'integration (mvn test)
echo   [3] Recompiler proprement le projet
echo   [4] Afficher les URLs et les commandes curl
echo   [0] Quitter
echo.
set "choix="
set /p "choix=Votre choix : "
if "%choix%"=="1" goto :lancer
if "%choix%"=="2" goto :tests
if "%choix%"=="3" goto :compile
if "%choix%"=="4" goto :urls
if "%choix%"=="0" goto :fin
goto :menu

:lancer
cls
echo ==============================================================================
echo   L'application demarre sur le port 8082.
echo   Appuyez sur Ctrl+C dans cette fenetre pour l'arreter.
echo ==============================================================================
echo.
echo   Swagger UI   : http://localhost:8082/swagger-ui.html
echo   OpenAPI JSON : http://localhost:8082/v3/api-docs
echo   Comptes JSON : http://localhost:8082/banque/comptes
echo   Console H2   : http://localhost:8082/h2-console
echo.
call mvn -B -q spring-boot:run
goto :suspension

:tests
cls
echo Execution de la suite de tests (JSON, XML, CRUD, filtres, virements)...
echo.
call mvn -B test
if errorlevel 1 goto :echec
goto :suspension

:compile
cls
call mvn -B -q clean compile
if errorlevel 1 goto :echec
echo.
echo Compilation terminee sans erreur.
goto :suspension

:urls
cls
echo ==============================================================================
echo   URLS DE L'API  (application lancee, choix 1)
echo ==============================================================================
echo.
echo   Documentation
echo     Swagger UI    http://localhost:8082/swagger-ui.html
echo     OpenAPI JSON  http://localhost:8082/v3/api-docs
echo     Console H2    http://localhost:8082/h2-console
echo.
echo   CRUD des comptes
echo     GET     http://localhost:8082/banque/comptes
echo     GET     http://localhost:8082/banque/comptes/1
echo     POST    http://localhost:8082/banque/comptes
echo     PUT     http://localhost:8082/banque/comptes/1
echo     DELETE  http://localhost:8082/banque/comptes/1
echo.
echo   Filtres et virement
echo     GET     http://localhost:8082/banque/comptes/type/EPARGNE
echo     GET     http://localhost:8082/banque/comptes/riches?seuil=1000
echo     POST    http://localhost:8082/banque/virements
echo.
echo ------------------------------------------------------------------------------
echo   Exemples curl
echo ------------------------------------------------------------------------------
echo.
echo   Liste JSON
echo     curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/json"
echo.
echo   Liste XML
echo     curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/xml"
echo.
echo   Creation JSON
echo     curl -X POST "http://localhost:8082/banque/comptes" ^
echo       -H "Content-Type: application/json" ^
echo       -d "{\"solde\": 1500.0, \"dateCreation\": \"2024-11-01\", \"type\": \"COURANT\"}"
echo.
echo   Creation XML
echo     curl -X POST "http://localhost:8082/banque/comptes" ^
echo       -H "Content-Type: application/xml" ^
echo       -d "<compte><solde>1500.0</solde><dateCreation>2024-11-01</dateCreation><type>COURANT</type></compte>"
echo.
echo   Virement
echo     curl -X POST "http://localhost:8082/banque/virements" ^
echo       -H "Content-Type: application/json" ^
echo       -d "{\"compteSource\": 1, \"compteDestination\": 2, \"montant\": 100.0}"
echo.
goto :suspension

:echec
echo.
echo *** Echec de la compilation ou des tests. ***
:suspension
echo.
echo ==============================================================================
echo   Fin de l'operation. Appuyez sur une touche pour revenir au menu.
echo ==============================================================================
pause >nul
goto :menu

:fin
exit /b 0
