@echo off
setlocal
cd /d "%~dp0"
title Kotoba - Local Translator Setup

where py >nul 2>nul
if errorlevel 1 goto missing_python
py -3.11 --version >nul 2>nul
if errorlevel 1 goto missing_python

if not exist ".venv\Scripts\python.exe" (
  echo Creating the private translator environment...
  py -3.11 -m venv .venv
  if errorlevel 1 goto failed
)

echo Installing the local translation engine. This can take several minutes...
".venv\Scripts\python.exe" -m pip install --disable-pip-version-check -r requirements.txt
if errorlevel 1 goto failed

set "ARGOS_DEVICE_TYPE=cpu"
set "ARGOS_COMPUTE_TYPE=int8"
set "ARGOS_INTER_THREADS=1"
set "ARGOS_INTRA_THREADS=1"
set "ARGOS_PACKAGES_DIR=%~dp0models"
".venv\Scripts\python.exe" install-model.py
if errorlevel 1 goto failed

echo.
echo Preparing Kotoba Client settings...
".venv\Scripts\python.exe" configure-minecraft.py
if errorlevel 1 goto failed

echo.
echo Setup complete. Next, run 2-Start-Local-Translator.bat before Minecraft.
pause
exit /b 0

:missing_python
echo Python 3.11 is required for the one-time setup.
echo Open https://www.python.org/downloads/windows/ and install Python 3.11.
echo Then run this setup file again. Python is only used by the local translator.
start "" "https://www.python.org/downloads/windows/"
pause
exit /b 1

:failed
echo.
echo Setup did not finish. Check the message above, then run this file again.
pause
exit /b 1
