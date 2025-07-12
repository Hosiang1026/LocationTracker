@echo off
chcp 65001 >nul

REM LocationTracker 打包脚本 (Windows版本)
REM 支持打包 release 和 debug 版本

echo 🚀 LocationTracker 打包脚本
echo ================================

REM 检查是否在正确的目录
if not exist "app\build.gradle" (
    echo ❌ 错误: 请在项目根目录运行此脚本
    pause
    exit /b 1
)

REM 获取版本信息
for /f "tokens=2 delims= " %%i in ('findstr "versionName" app\build.gradle') do set VERSION_NAME=%%i
set VERSION_NAME=%VERSION_NAME:"=%

for /f "tokens=2 delims= " %%i in ('findstr "versionCode" app\build.gradle') do set VERSION_CODE=%%i

echo 📱 版本信息:
echo    版本号: %VERSION_NAME%
echo    版本代码: %VERSION_CODE%
echo.

REM 创建输出目录
set OUTPUT_DIR=build_outputs
if not exist %OUTPUT_DIR% mkdir %OUTPUT_DIR%

REM 清理之前的构建
echo 🧹 清理之前的构建...
call gradlew.bat clean

REM 打包函数
:build_apk
set build_type=%1
set output_name=%2

echo 📦 开始打包 %build_type% 版本...

REM 执行打包
call gradlew.bat assemble%build_type%

if %errorlevel% equ 0 (
    REM 复制APK到输出目录
    copy "app\build\outputs\apk\%build_type%\app-%build_type%.apk" "%OUTPUT_DIR%\%output_name%"
    echo ✅ %build_type% 版本打包成功: %OUTPUT_DIR%\%output_name%
    
    REM 显示APK信息
    echo 📊 APK 信息:
    dir "%OUTPUT_DIR%\%output_name%"
    echo.
) else (
    echo ❌ %build_type% 版本打包失败
    pause
    exit /b 1
)
goto :eof

REM 显示菜单
echo 请选择打包类型:
echo 1) 打包 Release 版本 (带版本号)
echo 2) 打包 Debug 版本  
echo 3) 打包两个版本
echo 4) 退出
echo.

set /p choice=请输入选择 (1-4): 

if "%choice%"=="1" (
    call :build_apk Release "LocationTracker-v%VERSION_NAME%-release.apk"
) else if "%choice%"=="2" (
    call :build_apk Debug "LocationTracker-v%VERSION_NAME%-debug.apk"
) else if "%choice%"=="3" (
    echo 📦 打包两个版本...
    call :build_apk Release "LocationTracker-v%VERSION_NAME%-release.apk"
    call :build_apk Debug "LocationTracker-v%VERSION_NAME%-debug.apk"
) else if "%choice%"=="4" (
    echo 👋 退出打包脚本
    pause
    exit /b 0
) else (
    echo ❌ 无效选择，请重新运行脚本
    pause
    exit /b 1
)

echo.
echo 🎉 打包完成!
echo 📁 输出目录: %OUTPUT_DIR%
echo 📋 文件列表:
dir %OUTPUT_DIR%

echo.
echo 📱 包名信息:
echo    Release版本包名: com.ljs.locationtracker.v%VERSION_NAME%
echo    Debug版本包名: com.ljs.locationtracker.debug
echo.
echo 💡 提示:
echo    - Release版本已签名，可直接发布
echo    - Debug版本用于测试，未签名
echo    - 两个版本可以同时安装，包名不同

pause 