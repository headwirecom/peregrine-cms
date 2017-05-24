@echo off
setlocal
set path=%path%;%~dp0vips\bin
set path
cd resources 
start java -jar org.apache.sling.launchpad-9-SNAPSHOT.jar

rem -- loop until sling server is present and running on port 8080
:while1
sleep 1
wget -q -O testserver.html http://localhost:8080/content/sites/example.html
if %ERRORLEVEL% NEQ 0 (
            echo waiting for sling server to come up
            goto :while1
)
del testserver.html
rem -- open file with redirect to localhost:8080
index.html
endlocal