@echo off
setlocal
cd /d "%~dp0"
title Kotoba Local Translator

if not exist ".venv\Scripts\python.exe" (
  echo Run 1-Setup-Local-Translator.bat first.
  pause
  exit /b 1
)

set "ARGOS_DEVICE_TYPE=cpu"
set "ARGOS_COMPUTE_TYPE=int8"
set "ARGOS_INTER_THREADS=1"
set "ARGOS_INTRA_THREADS=1"
set "ARGOS_PACKAGES_DIR=%~dp0models"
".venv\Scripts\python.exe" server.py
if errorlevel 1 pause
