@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

call "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.cmd" %*

