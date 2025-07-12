#!/bin/bash

# LocationTracker 打包脚本
# 支持打包 release 和 debug 版本

echo "🚀 LocationTracker 打包脚本"
echo "================================"

# 检查是否在正确的目录
if [ ! -f "app/build.gradle" ]; then
    echo "❌ 错误: 请在项目根目录运行此脚本"
    exit 1
fi

# 获取版本信息
VERSION_NAME=$(grep 'versionName' app/build.gradle | sed 's/.*versionName "\(.*\)".*/\1/')
VERSION_CODE=$(grep 'versionCode' app/build.gradle | sed 's/.*versionCode \([0-9]*\).*/\1/')

echo "📱 版本信息:"
echo "   版本号: $VERSION_NAME"
echo "   版本代码: $VERSION_CODE"
echo ""

# 创建输出目录
OUTPUT_DIR="build_outputs"
mkdir -p $OUTPUT_DIR

# 清理之前的构建
echo "🧹 清理之前的构建..."
./gradlew clean

# 打包函数
build_apk() {
    local build_type=$1
    local output_name=$2
    
    echo "📦 开始打包 $build_type 版本..."
    
    # 执行打包
    ./gradlew assemble$build_type
    
    if [ $? -eq 0 ]; then
        # 复制APK到输出目录
        cp app/build/outputs/apk/$build_type/app-$build_type.apk $OUTPUT_DIR/$output_name
        echo "✅ $build_type 版本打包成功: $OUTPUT_DIR/$output_name"
        
        # 显示APK信息
        echo "📊 APK 信息:"
        ls -lh $OUTPUT_DIR/$output_name
        echo ""
    else
        echo "❌ $build_type 版本打包失败"
        exit 1
    fi
}

# 显示菜单
echo "请选择打包类型:"
echo "1) 打包 Release 版本 (带版本号)"
echo "2) 打包 Debug 版本"
echo "3) 打包两个版本"
echo "4) 退出"
echo ""

read -p "请输入选择 (1-4): " choice

case $choice in
    1)
        build_apk "Release" "LocationTracker-v${VERSION_NAME}-release.apk"
        ;;
    2)
        build_apk "Debug" "LocationTracker-v${VERSION_NAME}-debug.apk"
        ;;
    3)
        echo "📦 打包两个版本..."
        build_apk "Release" "LocationTracker-v${VERSION_NAME}-release.apk"
        build_apk "Debug" "LocationTracker-v${VERSION_NAME}-debug.apk"
        ;;
    4)
        echo "👋 退出打包脚本"
        exit 0
        ;;
    *)
        echo "❌ 无效选择，请重新运行脚本"
        exit 1
        ;;
esac

echo ""
echo "🎉 打包完成!"
echo "📁 输出目录: $OUTPUT_DIR"
echo "📋 文件列表:"
ls -la $OUTPUT_DIR/

echo ""
echo "📱 包名信息:"
echo "   Release版本包名: com.ljs.locationtracker.v${VERSION_NAME}"
echo "   Debug版本包名: com.ljs.locationtracker.debug"
echo ""
echo "💡 提示:"
echo "   - Release版本已签名，可直接发布"
echo "   - Debug版本用于测试，未签名"
echo "   - 两个版本可以同时安装，包名不同" 