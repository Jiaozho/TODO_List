@echo off
setlocal

set WRAPPER_DIR=%~dp0
set WRAPPER_DIR=%WRAPPER_DIR:~0,-1%
set BASE_DIR=%WRAPPER_DIR%\..\..
for %%i in ("%BASE_DIR%") do set BASE_DIR=%%~fi

set PROPS_FILE=%WRAPPER_DIR%\maven-wrapper.properties
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_URL=
set DISTRIBUTION_URL=
for /f "usebackq tokens=1* delims==" %%A in ("%PROPS_FILE%") do (
  if "%%A"=="distributionUrl" set DISTRIBUTION_URL=%%B
  if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)

if not exist "%WRAPPER_JAR%" (
  if not defined WRAPPER_URL (
    echo wrapperUrl is not set in %PROPS_FILE%
    exit /b 1
  )
  echo Downloading Maven Wrapper from %WRAPPER_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

set MAVEN_OPTS=%MAVEN_OPTS%
set MVNW_VERBOSE=false

set CMD_LINE_ARGS=%*

set JAVA_EXE=java
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
)

%JAVA_EXE% %MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %CMD_LINE_ARGS%

endlocal
