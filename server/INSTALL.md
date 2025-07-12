# LocationTracker 私人服务器配置方案

## 🎯 方案概述

通过私人服务器实现LocationTracker应用的远程配置管理，支持：
- ✅ 实时配置下发
- ✅ Web管理界面
- ✅ 自动配置同步
- ✅ 完全免费使用

## 📋 系统要求

### 服务器要求
- **操作系统**: Linux (Ubuntu 18.04+ / CentOS 7+)
- **Web服务器**: Apache 2.4+ 或 Nginx 1.18+
- **PHP**: 7.4+ (推荐8.0+)
- **存储空间**: 至少100MB
- **网络**: 支持HTTPS

### 客户端要求
- **Android版本**: 4.0+ (API 14+)
- **网络权限**: 需要INTERNET权限
- **存储权限**: 用于保存配置缓存

## 🚀 快速部署

### 1. 准备服务器环境

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装Apache和PHP
sudo apt install apache2 php php-json php-curl -y

# 启用必要的Apache模块
sudo a2enmod rewrite
sudo a2enmod headers
sudo systemctl restart apache2
```

### 2. 部署配置文件

```bash
# 创建项目目录
sudo mkdir -p /var/www/html/locationtracker
cd /var/www/html/locationtracker

# 上传文件（使用提供的deploy.sh脚本）
./deploy.sh your-server.com root /var/www/html/locationtracker

# 设置权限
sudo chown -R www-data:www-data /var/www/html/locationtracker
sudo chmod 755 /var/www/html/locationtracker
sudo chmod 644 /var/www/html/locationtracker/*.php
sudo chmod 644 /var/www/html/locationtracker/*.html
sudo chmod 644 /var/www/html/locationtracker/*.json
```

### 3. 配置SSL证书（推荐）

```bash
# 安装Certbot
sudo apt install certbot python3-certbot-apache -y

# 获取SSL证书
sudo certbot --apache -d your-server.com

# 自动续期
sudo crontab -e
# 添加: 0 12 * * * /usr/bin/certbot renew --quiet
```

### 4. 测试部署

```bash
# 测试Web界面
curl -I https://your-server.com/

# 测试API接口
curl https://your-server.com/api/config
```

## 📱 Android应用集成

### 1. 更新配置URL

在 `ConfigSyncService.java` 中更新服务器地址：

```java
private static final String CONFIG_URL = "https://your-server.com/api/config";
```

### 2. 编译并安装应用

```bash
cd LocationTracker
./gradlew assembleRelease
```

### 3. 测试配置同步

1. 安装应用到设备
2. 启动应用
3. 在Web界面修改配置
4. 等待5分钟后检查应用是否自动应用新配置

## 🔧 高级配置

### 自定义同步间隔

修改 `ConfigSyncService.java` 中的同步间隔：

```java
private static final int SYNC_INTERVAL = 300000; // 5分钟
```

### 添加认证机制

在 `config_api.php` 中添加基本认证：

```php
// 添加在文件开头
if (!isset($_SERVER['PHP_AUTH_USER']) || 
    $_SERVER['PHP_AUTH_USER'] !== 'admin' || 
    $_SERVER['PHP_AUTH_PW'] !== 'your-password') {
    header('WWW-Authenticate: Basic realm="LocationTracker Config"');
    header('HTTP/1.0 401 Unauthorized');
    echo json_encode(['error' => '认证失败']);
    exit;
}
```

### 数据库存储（可选）

如果需要使用数据库存储配置，修改 `config_api.php`：

```php
// 数据库配置
$db_host = 'localhost';
$db_name = 'locationtracker';
$db_user = 'username';
$db_pass = 'password';

try {
    $pdo = new PDO("mysql:host=$db_host;dbname=$db_name", $db_user, $db_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    die(json_encode(['error' => '数据库连接失败']));
}
```

## 🔍 故障排除

### 常见问题

#### 1. 配置同步失败
**症状**: Android应用无法获取配置
**解决方案**:
```bash
# 检查API接口
curl -v https://your-server.com/api/config

# 检查PHP错误日志
sudo tail -f /var/log/apache2/error.log

# 检查文件权限
ls -la /var/www/html/locationtracker/
```

#### 2. Web界面无法访问
**症状**: 浏览器显示404或500错误
**解决方案**:
```bash
# 检查Apache配置
sudo apache2ctl configtest

# 检查mod_rewrite是否启用
sudo a2enmod rewrite
sudo systemctl restart apache2

# 检查.htaccess文件
cat /var/www/html/locationtracker/.htaccess
```

#### 3. SSL证书问题
**症状**: HTTPS访问失败
**解决方案**:
```bash
# 检查SSL证书状态
sudo certbot certificates

# 重新获取证书
sudo certbot --apache -d your-server.com

# 检查防火墙设置
sudo ufw status
```

### 日志监控

#### 服务器日志
```bash
# Apache访问日志
sudo tail -f /var/log/apache2/access.log

# Apache错误日志
sudo tail -f /var/log/apache2/error.log

# PHP错误日志
sudo tail -f /var/log/php7.4-fpm.log
```

#### Android应用日志
```bash
# 查看应用日志
adb logcat | grep "ConfigSyncService"

# 查看配置同步状态
adb shell dumpsys activity services | grep ConfigSync
```

## 🔒 安全建议

### 1. 网络安全
- ✅ 启用HTTPS
- ✅ 配置防火墙规则
- ✅ 定期更新系统
- ✅ 使用强密码

### 2. 应用安全
- ✅ 验证配置数据
- ✅ 限制API访问频率
- ✅ 监控异常访问
- ✅ 定期备份配置

### 3. 数据保护
- ✅ 加密敏感配置
- ✅ 定期清理日志
- ✅ 监控磁盘使用
- ✅ 设置访问限制

## 📊 性能优化

### 1. 服务器优化
```bash
# 启用PHP OPcache
sudo apt install php-opcache
sudo systemctl restart apache2

# 配置Apache缓存
sudo a2enmod expires
sudo systemctl restart apache2
```

### 2. 应用优化
- 调整同步间隔
- 实现增量同步
- 添加重试机制
- 优化网络请求

## 🔄 维护指南

### 日常维护
1. **监控服务器状态**
   ```bash
   # 检查服务状态
   sudo systemctl status apache2
   sudo systemctl status php7.4-fpm
   
   # 检查磁盘使用
   df -h
   
   # 检查内存使用
   free -h
   ```

2. **备份配置文件**
   ```bash
   # 创建备份脚本
   sudo cp /var/www/html/locationtracker/locationtracker_config.json \
          /backup/locationtracker_config_$(date +%Y%m%d_%H%M%S).json
   ```

3. **更新应用版本**
   - 定期更新Android应用
   - 测试新功能兼容性
   - 备份用户配置

### 故障恢复
1. **服务器故障**
   - 检查服务状态
   - 查看错误日志
   - 重启相关服务
   - 恢复配置文件

2. **应用故障**
   - 检查网络连接
   - 验证服务器地址
   - 清除应用缓存
   - 重新安装应用

## 📞 技术支持

### 获取帮助
- 📧 邮件支持: your-email@example.com
- 🐛 问题反馈: GitHub Issues
- 📖 文档更新: 定期检查README

### 社区支持
- 💬 技术讨论: 相关技术论坛
- 🔧 代码贡献: Pull Request
- 📢 功能建议: Feature Request

---

**注意**: 使用本方案时请遵守相关法律法规，确保在合法范围内使用。 