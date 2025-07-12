# LocationTracker 多设备配置管理系统

## 🎯 系统概述

这是一个支持多设备管理的LocationTracker配置系统，每台设备可以有不同的Webhook URL，并且能够实时监控设备在线状态。

### 主要功能
- ✅ **多设备支持**: 每台设备独立配置
- ✅ **在线监控**: 10分钟心跳检测设备状态
- ✅ **Web管理界面**: 可视化设备管理
- ✅ **实时同步**: 配置修改后自动下发
- ✅ **设备区分**: 使用设备唯一ID区分

## 📱 设备端配置

### 1. 设备ID生成
系统使用Android设备的唯一标识符作为设备ID：
```java
String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
```

### 2. 配置同步流程
1. **启动时**: 应用启动后自动启动配置同步服务
2. **定期同步**: 每5分钟从服务器获取配置
3. **心跳发送**: 每5分钟发送心跳到服务器
4. **配置应用**: 检测到配置变化时自动重启位置服务

### 3. 配置参数
- **Webhook URL**: 每台设备可以配置不同的上报地址
- **更新间隔**: 位置上报的时间间隔（10-10800秒）
- **通知开关**: 控制前台通知显示（APP默认自动启动）

## 🌐 服务器端API

### API接口说明

#### 1. 获取设备列表
```
GET /api/config
```
返回所有设备的列表和在线状态

#### 2. 获取设备配置
```
GET /api/config/{device_id}
```
返回指定设备的配置信息

#### 3. 更新设备配置
```
POST /api/config/{device_id}
Content-Type: application/json

{
  "device_name": "我的手机",
  "webhook_url": "https://your-server.com/webhook/xxx",
  "update_interval": 600,
  "notification_enabled": true
}
```

#### 4. 设备心跳
```
POST /api/config/heartbeat
Device-ID: {device_id}
```
设备定期发送心跳保持在线状态

#### 5. 删除设备
```
DELETE /api/config/{device_id}
```
删除指定设备的配置

### 响应格式

#### 设备列表响应
```json
{
  "devices": [
    {
      "device_id": "abc123def456",
      "device_name": "我的手机",
      "is_online": true,
      "last_seen": "2025-07-05T18:30:00Z",
      "webhook_url": "https://your-server.com/webhook/xxx",
      "update_interval": 600
    }
  ],
  "total_devices": 1,
  "online_devices": 1
}
```

#### 设备配置响应
```json
{
  "device_id": "abc123def456",
  "device_name": "我的手机",
  "webhook_url": "https://your-server.com/webhook/xxx",
  "update_interval": 600,
  "notification_enabled": true,
  "is_online": true,
  "last_seen": "2025-07-05T18:30:00Z",
  "last_updated": "2025-07-05T18:30:00Z",
  "server_time": 1751712600
}
```

## 🖥️ Web管理界面

### 功能特性
- 📊 **统计面板**: 显示总设备数、在线设备数、离线设备数
- 📱 **设备卡片**: 每个设备显示为一个卡片，包含状态、配置信息
- ✏️ **编辑功能**: 点击编辑按钮可以修改设备配置
- 🗑️ **删除功能**: 可以删除不需要的设备
- 🔄 **自动刷新**: 每30秒自动刷新设备状态

### 界面说明
1. **设备状态**: 绿色边框表示在线，红色边框表示离线
2. **最后在线**: 显示设备最后一次发送心跳的时间
3. **配置信息**: 显示设备的Webhook URL和更新间隔
4. **操作按钮**: 编辑和删除按钮

## 🔧 部署步骤

### 1. 服务器准备
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

### 2. 部署文件
```bash
# 使用部署脚本
./deploy.sh your-server.com root /var/www/html/locationtracker

# 设置权限
sudo chown -R www-data:www-data /var/www/html/locationtracker
sudo chmod 755 /var/www/html/locationtracker
sudo chmod 644 /var/www/html/locationtracker/*.php
sudo chmod 644 /var/www/html/locationtracker/*.html
sudo chmod 644 /var/www/html/locationtracker/*.json
```

### 3. 配置SSL（推荐）
```bash
# 安装Certbot
sudo apt install certbot python3-certbot-apache -y

# 获取SSL证书
sudo certbot --apache -d your-server.com
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
private static final String HEARTBEAT_URL = "https://your-server.com/api/config/heartbeat";
```

### 2. 编译应用
```bash
cd LocationTracker
./gradlew assembleRelease
```

### 3. 安装测试
1. 安装应用到设备
2. 启动应用
3. 在Web界面查看设备是否自动注册
4. 修改设备配置测试同步功能

## 🔍 故障排除

### 常见问题

#### 1. 设备不显示在列表中
**可能原因**:
- 设备ID获取失败
- 网络连接问题
- 服务器配置错误

**解决方案**:
```bash
# 检查设备ID
adb shell settings get secure android_id

# 检查网络连接
curl -v https://your-server.com/api/config

# 检查服务器日志
sudo tail -f /var/log/apache2/error.log
```

#### 2. 配置同步失败
**可能原因**:
- API接口错误
- 设备ID不匹配
- 网络超时

**解决方案**:
```bash
# 检查API响应
curl -H "Device-ID: your-device-id" https://your-server.com/api/config/your-device-id

# 检查设备配置
adb shell dumpsys activity services | grep ConfigSync
```

#### 3. 心跳发送失败
**可能原因**:
- 网络连接不稳定
- 服务器响应慢
- 设备ID格式错误

**解决方案**:
```bash
# 测试心跳接口
curl -X POST -H "Device-ID: your-device-id" https://your-server.com/api/config/heartbeat

# 检查网络连接
ping your-server.com
```

### 日志查看

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
# 查看配置同步日志
adb logcat | grep "ConfigSyncService"

# 查看心跳日志
adb logcat | grep "心跳"

# 查看网络请求日志
adb logcat | grep "OkHttp"
```

## 🔒 安全建议

### 1. 网络安全
- ✅ 启用HTTPS
- ✅ 配置防火墙
- ✅ 定期更新系统
- ✅ 监控异常访问

### 2. 数据保护
- ✅ 定期备份设备配置
- ✅ 监控磁盘使用
- ✅ 清理过期日志
- ✅ 设置访问限制

### 3. 应用安全
- ✅ 验证设备ID
- ✅ 限制API访问频率
- ✅ 监控异常行为
- ✅ 加密敏感数据

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
1. **监控设备状态**: 定期检查设备在线情况
2. **备份配置数据**: 定期备份设备配置文件
3. **清理离线设备**: 删除长期离线的设备配置
4. **更新应用版本**: 定期更新Android应用

### 故障恢复
1. **服务器故障**: 检查服务状态，重启相关服务
2. **应用故障**: 检查网络连接，重新安装应用
3. **配置丢失**: 从备份恢复配置文件
4. **设备异常**: 检查设备ID，重新注册设备

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

**注意**: 使用本系统时请遵守相关法律法规，确保在合法范围内使用。 