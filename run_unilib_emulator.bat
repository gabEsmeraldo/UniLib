@echo off
setlocal

set "PROJECT_DIR=C:\Users\gabzu\Documents\unilib"
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "PACKAGE_NAME=com.example.unilib"

if not exist "%ADB%" (
    echo adb not found at:
    echo %ADB%
    echo.
    echo Check your Android SDK installation.
    pause
    exit /b 1
)

echo Checking emulator connection...
for /f "tokens=1" %%D in ('"%ADB%" devices ^| findstr /R "^emulator-[0-9][0-9]*[ ]*device"') do (
    set "EMULATOR_FOUND=1"
)

if not defined EMULATOR_FOUND (
    echo No running emulator detected.
    echo Open an emulator in Android Studio and run this file again.
    pause
    exit /b 1
)

echo Emulator detected.
echo Installing debug build...
pushd "%PROJECT_DIR%"
call gradlew.bat installDebug
if errorlevel 1 (
    popd
    echo.
    echo Install failed.
    pause
    exit /b 1
)
popd

echo Launching app...
"%ADB%" shell monkey -p %PACKAGE_NAME% -c android.intent.category.LAUNCHER 1
if errorlevel 1 (
    echo.
    echo App launch failed.
    pause
    exit /b 1
)

echo.
echo UniLib installed and launched successfully.
pause
