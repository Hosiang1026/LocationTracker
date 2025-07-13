<p align="right">
  <b><a href="./README.md">Chinese</a></b> | <b>English</b>
</p>

<br>

# LocationTracker

[![Android Version](https://img.shields.io/badge/Android-4.0+-green.svg)](https://developer.android.com/about/versions/android-4.0)
[![Version](https://img.shields.io/badge/Version-V2.1.5-blue.svg)](https://github.com/your-repo/LocationTracker/releases)
[![UI Design](https://img.shields.io/badge/UI-Single%20Panel%20Design-brightgreen.svg)](https://haoxiang.eu.org/ui/UI_PREVIEW)
[![Tablet UI](https://img.shields.io/badge/Tablet%20UI-Large%20Screen%20Optimized-blue.svg)](https://haoxiang.eu.org/ui/TABLET_UI_PREVIEW)
[![Theme Compatibility](https://img.shields.io/badge/Theme-Smart%20Compatibility-orange.svg)](https://github.com/your-repo/LocationTracker#-theme-compatibility-fix-details)

> 📱 **Smart Location Reporting App** - Send location data to Home Assistant or other servers via HTTP Webhook (Pure HTTP Webhook application, no MQTT functionality)

## 🚨 Important Notice

> 🚨 **Anti-Fraud Statement**: This app is completely open source and free. Please do not believe any paid versions or paid services!
> 
> - 📱 **Official Channel**: Download only from the official GitHub repository
> - 💰 **Completely Free**: All features are completely free, no paid items
> - 🔒 **Open Source Transparent**: Code is completely open source, freely viewable and modifiable
> - ⚠️ **Beware of Scams**: If you encounter payment requests, please report immediately and block
> - 📞 **Official Support**: If you have questions, please provide feedback through GitHub Issues

---

## 🚀 Quick Start

### 📥 Installation
1. Download [APK file](./app/build/outputs/apk/release/)
2. Install on Android device
3. Grant necessary permissions
4. Configure Webhook URL (supports http/https, format validation, cannot be empty)
5. Configure reporting cycle (10~10800 seconds, cannot be empty, range validation)
6. Click "Start Location" to use

### ⚡ Core Features
- 📍 **Real-time Location**: GPS/network positioning, precise location reporting
- 🔄 **Data Deduplication**: Avoid repeated reporting of identical location data
- 🔋 **Low Battery Protection**: Automatically pause reporting when battery is below 10%
- 🌙 **Background Keep-alive**: Auto-start on boot, service auto-restart
- 🎨 **Transparent Status Bar**: Support Android 4.4+ transparent status bar
- 🛡️ **Global Exception Capture**: Automatic crash log recording and friendly prompts
- 📝 **Input Validation**: Strict validation of Webhook URL format and reporting cycle
- 🖥️ **Multi-resolution Adaptation**: Support different screen sizes
- 🔧 **Device Optimization Guide**: Built-in optimization settings guide for various device brands
- 🔒 **SQL Injection Protection**: Use parameterized queries to prevent SQL injection

## 🎨 Interface Preview

> 💡 **Quick Preview**: [📱 Mobile Interface](https://haoxiang.eu.org/ui/UI_PREVIEW) | [📟 Tablet Interface](https://haoxiang.eu.org/ui/TABLET_UI_PREVIEW)

### 📱 Mobile Features
- **Single Panel Design**: Bottom TAB switching, clean and unified interface
- **Status Monitoring**: Real-time display of connection status, location status, battery level, report count
- **Configuration Panel**: Webhook URL configuration, reporting interval settings, notification toggle
- **Runtime Logs**: Real-time display of app running status and reporting records

### 📟 Tablet Features
- **Large Screen Optimization**: Layout and font size optimized for tablet devices
- **Touch Friendly**: Larger buttons and interaction areas
- **Information Density**: Reasonable information density, making full use of large screen space
- **Landscape/Portrait Adaptation**: Support landscape/portrait switching, maintaining good user experience

## 🚀 User Guide

### First Time Use
1. **Install App**: Download and install APK file
2. **Grant Permissions**: Allow location, network, auto-start and other permissions
3. **Configure Parameters**: Set Webhook URL and reporting interval in configuration panel
4. **Start Service**: Click "Start Location" button to start location reporting
5. **Monitor Status**: Switch to monitoring panel to view running status and logs

### Interface Operations
- **📊 Monitoring Panel**: View connection status, location status, battery level, report count
- **⚙️ Configuration Panel**: Set Webhook URL, reporting interval, notification toggle
- **📱 Bottom Navigation**: Switch between monitoring and configuration panels
- **🛠️ Log Management**: Click "🛠️ Logs" to view crash logs
- **🔧 Optimization Settings**: Click "Optimization Settings" to get device optimization suggestions

### Daily Use
- **Auto Start**: App will automatically start location reporting service
- **Background Running**: Service runs continuously in background, no manual intervention needed
- **Status Monitoring**: View connection status and report count through status panel
- **Log Viewing**: View detailed running logs for troubleshooting
- **Notification Monitoring**: View current reporting time and status through notification bar

### Troubleshooting
1. **Service Cannot Start**: Check if GPS is enabled, permissions are granted
2. **Data Reporting Failed**: Check network connection, whether Webhook URL is correct
3. **Background Killed**: Check device optimization settings, ensure app is not restricted
4. **Battery Drain Too Fast**: Increase reporting interval appropriately, or enable low battery protection
5. **Theme Compatibility Issues**: App has built-in smart theme adaptation, if problems persist please check crash logs

## 📋 Permission Description

### Required Permissions
- `ACCESS_FINE_LOCATION`: Precise location permission
- `ACCESS_COARSE_LOCATION`: Coarse location permission
- `ACCESS_BACKGROUND_LOCATION`: Background location permission (Android 10+)
- `INTERNET`: Internet access permission
- `ACCESS_NETWORK_STATE`: Network state access permission
- `WAKE_LOCK`: Wake lock permission
- `RECEIVE_BOOT_COMPLETED`: Boot auto-start permission

### Optional Permissions
- `FOREGROUND_SERVICE`: Foreground service permission (Android 8.0+)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Ignore battery optimization permission
- `SYSTEM_ALERT_WINDOW`: System overlay permission (Huawei/Honor devices)
- `ACCESS_WIFI_STATE`: WiFi state access permission (for network positioning)
- `CHANGE_WIFI_STATE`: WiFi state modification permission
- `ACCESS_LOCATION_EXTRA_COMMANDS`: Location extra commands permission
- `READ_EXTERNAL_STORAGE`: External storage read permission (for crash log management)
- `WRITE_EXTERNAL_STORAGE`: External storage write permission (for crash log management)
- `READ_PHONE_STATE`: Phone state read permission
- `WRITE_SETTINGS`: System settings write permission
- `BLUETOOTH`: Bluetooth permission
- `BLUETOOTH_ADMIN`: Bluetooth admin permission

## 🔒 Security Analysis

### Fixed Security Issues
✅ **SQL Injection Protection**: All database operations use parameterized queries
✅ **Network Request Security**: Use OkHttp library, support HTTPS, include timeout and retry mechanisms
✅ **Permission Management**: Comprehensive runtime permission application mechanism
✅ **Input Validation**: Strict validation of user input
✅ **API Level Compatibility**: Fixed all API level compatibility issues

### Security Recommendations
- 🔐 Recommend using HTTPS Webhook URL
- 🔐 Regularly update app version
- 🔐 Use in trusted network environment
- 🔐 Regularly check accuracy of reported data

## ⚙️ Configuration Description

### Basic Configuration
1. **Webhook URL**: Must start with http/https, cannot be empty, format validation on input
2. **Reporting Interval**: 10~10800 seconds, cannot be empty, invalid if out of range
3. **Notification Toggle**: Control foreground notification display, keep app running in background

### Data Format

The app will send different format data according to different reporting scenarios:

#### Standard Location Reporting Format
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "altitude": 100.5,
  "gps_accuracy": 5.0,
  "battery": 75,
  "speed": 30.0,
  "bearing": 180.0,
  "timestamp": 1640995200000,
  "provider": "gps",
  "screen_off": false,
  "power_save_mode": false
}
```

#### Immediate Reporting Format (with extra flag)
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "altitude": 100.5,
  "gps_accuracy": 5.0,
  "battery": 75,
  "speed": 30.0,
  "bearing": 180.0,
  "timestamp": 1640995200000,
  "provider": "gps",
  "screen_off": false,
  "power_save_mode": false,
  "immediate_report": true
}
```

#### WorkManager Simplified Format (basic fields only)
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "altitude": 100.5,
  "accuracy": 5.0,
  "timestamp": 1640995200000
}
```

### Field Description

| Field Name | Type | Description | Required |
|------------|------|-------------|----------|
| `latitude` | number | Latitude | ✅ |
| `longitude` | number | Longitude | ✅ |
| `altitude` | number | Altitude (meters) | ⚠️ |
| `gps_accuracy` / `accuracy` | number | GPS accuracy (meters) | ⚠️ |
| `battery` | number | Battery level percentage | ⚠️ |
| `speed` | number | Movement speed (meters/second) | ⚠️ |
| `bearing` | number | Direction angle (0-360 degrees) | ⚠️ |
| `timestamp` | number | Timestamp (milliseconds) | ✅ |
| `provider` | string | Location provider (gps/network/passive) | ⚠️ |
| `screen_off` | boolean | Whether screen is off | ⚠️ |
| `power_save_mode` | boolean | Whether in power save mode | ⚠️ |
| `immediate_report` | boolean | Whether it's immediate report | ⚠️ |

**Note**: Only `latitude` and `longitude` are required fields, other fields may be empty or missing depending on device capabilities and system status.

## 📊 Compatibility Support

### 🖥️ Screen Sizes
| Device Type | Screen Size | Resolution Range | Interface Layout | Touch Optimization | Status Bar | Typical Device Examples |
|-------------|-------------|------------------|------------------|-------------------|------------|------------------------|
| **Small Phone** | 4.0-4.7 inches | 480x800-720x1280 | ✅ Compact Layout | ✅ Standard Size | ✅ Fully Transparent | Xiaomi 4 (5.0"), Huawei P8 (5.2"), Samsung Galaxy S5 (5.1") |
| **Standard Phone** | 5.0-6.0 inches | 720x1280-1080x1920 | ✅ Single Panel Design | ✅ Standard Size | ✅ Fully Transparent | Huawei P40 (6.1"), Xiaomi 12 (6.28"), OPPO Find X3 (6.7") |
| **Large Phone** | 6.1-6.7 inches | 1080x1920-1440x3200 | ✅ Single Panel Design | ✅ Standard Size | ✅ Fully Transparent | Huawei Mate 50 Pro (6.74"), Xiaomi 13 Ultra (6.73"), vivo X90 Pro+ (6.78") |
| **Small Tablet** | 7.0-8.0 inches | 1024x768-1200x1920 | ✅ Optimized Layout | ✅ Large Size Buttons | ✅ Fully Transparent | Huawei Tablet M6 (8.4"), Xiaomi Tablet 4 (8.0"), Samsung Galaxy Tab A (8.0") |
| **Standard Tablet** | 8.5-10.5 inches | 1200x1920-1600x2560 | ✅ Optimized Layout | ✅ Large Size Buttons | ✅ Fully Transparent | Huawei MatePad Pro (10.8"), Xiaomi Tablet 5 (11"), OPPO Pad (11") |
| **Large Tablet** | 11.0-12.9 inches | 1600x2560-2048x2732 | ✅ Large Screen Adaptation | ✅ Extra Large Size Buttons | ✅ Fully Transparent | Huawei MatePad Pro 12.6", Xiaomi Tablet 6 Pro (11") |
| **Foldable** | Unfolded 6.7-8.0 inches | 1080x1920-2208x1768 | ✅ Adaptive Layout | ✅ Smart Size | ✅ Fully Transparent | Samsung Galaxy Fold (7.6"), Huawei Mate X (8.0"), Xiaomi MIX Fold (8.01") |
| **Car/Large Screen** | 10.0-15.0 inches | 1920x1080-2560x1440 | ✅ Large Screen Adaptation | ✅ Extra Large Size Buttons | ✅ Fully Transparent | Car Android System, Smart Large Screen Devices, Android TV |

### 🤖 Android Versions
| Feature | Android 4.0-4.3 | Android 4.4-4.4W | Android 5.0+ | Android 10+ |
|---------|------------------|-------------------|--------------|-------------|
| **Basic Location** | ✅ Supported | ✅ Supported | ✅ Supported | ✅ Supported |
| **Background Location** | ✅ Supported | ✅ Supported | ✅ Supported | ⚠️ Permission Required |
| **Foreground Service** | ❌ Not Supported | ❌ Not Supported | ✅ Supported | ✅ Supported |
| **Transparent Status Bar** | ❌ Not Supported | ⚠️ Semi-transparent | ✅ Fully Transparent | ✅ Fully Transparent |
| **Theme Compatibility** | ✅ Smart Adaptation | ✅ Smart Adaptation | ✅ Smart Adaptation | ✅ Smart Adaptation |

### 📱 Device Brands
| Brand | Auto-start Permission | Background Keep-alive | Battery Optimization | Optimization Guide |
|-------|----------------------|----------------------|---------------------|-------------------|
| **Huawei/Honor** | ⚠️ Settings Required | ⚠️ Optimization Required | ⚠️ Ignore Required | ✅ Built-in Guide |
| **Xiaomi/Redmi** | ⚠️ Settings Required | ⚠️ Optimization Required | ⚠️ Ignore Required | ✅ Built-in Guide |
| **OPPO/OnePlus** | ⚠️ Settings Required | ⚠️ Optimization Required | ⚠️ Ignore Required | ✅ Built-in Guide |
| **vivo** | ⚠️ Settings Required | ⚠️ Optimization Required | ⚠️ Ignore Required | ✅ Built-in Guide |
| **Samsung** | ⚠️ Settings Required | ⚠️ Optimization Required | ⚠️ Ignore Required | ✅ Built-in Guide |

## 🏠 Home Assistant Integration

> 💡 **Recommended Solution**: Use with [TRSDM Dynamic Device Tracker](https://github.com/Dekadinious/trsdm_custom_device_tracker_for_home_assistant) plugin for more flexible device and attribute management

### Solution 1: TRSDM Dynamic Device Tracker (Recommended)

#### Installation Steps
1. Ensure [HACS](https://hacs.xyz/) (Home Assistant Community Store) is installed
2. In Home Assistant, go to HACS > Integrations
3. Click the "+" button, search for "TRSDM Dynamic Device Tracker"
4. Click install TRSDM Dynamic Device Tracker integration
5. Restart Home Assistant

#### Configuration Steps
1. In Home Assistant, go to Settings > Devices & Services
2. Click the "+" button to add new integration
3. Search for "TRSDM Dynamic Device Tracker" and select
4. Follow prompts to set up your first device tracker:
   - Give the tracker a name (e.g., "My Phone")
   - Integration will automatically generate unique webhook URL

#### Usage Method
1. **Configure Webhook URL**: Fill in Home Assistant's webhook URL in the APP's configuration panel
2. **Set Reporting Cycle**: Configure location reporting time interval (10-10800 seconds)
3. **Start Service**: Click "Start Location" button to start location reporting service
4. **Auto Report**: APP will automatically send location data to webhook URL according to configured cycle

**Example Configuration**:
- Webhook URL: `https://your-home-assistant-url/api/webhook/your-webhook-id`
- Reporting Cycle: `60` seconds
- Notification Toggle: `Enabled` (keep app running in background)

**Data Format**: APP will automatically send JSON data containing location information, only `latitude` and `longitude` fields are required, other fields will be automatically added based on device capabilities.

> ⚠️ **Important Reminder**: The reporting address must use a domain name or public IP address, not private IP addresses (such as 192.168.x.x, 10.x.x.x, etc.). Because mobile devices need to access the Home Assistant server through the internet, private IP addresses cannot be accessed in mobile network environments.

#### Standard Attributes
- Distance from home (meters and miles)
- Direction relative to home (towards, away_from, stationary)
- Bearing from home (N, NE, E, SE, S, SW, W, NW)
- Last update timestamp
- Last significant location change time (10m cumulative)

### Solution 2: Native Webhook Integration

#### Automation Configuration
```yaml
automation:
  - alias: "Location Update"
    trigger:
      platform: webhook
      webhook_id: your_webhook_id
    action:
      - service: device_tracker.see
        data:
          dev_id: myphone
          location_name: home
          latitude: "{{ trigger.json.latitude }}"
          longitude: "{{ trigger.json.longitude }}"
          gps_accuracy: "{{ trigger.json.gps_accuracy }}"
          battery: "{{ trigger.json.battery }}"
```

#### Device Tracker Configuration
```yaml
device_tracker:
  - platform: webhook
    webhook_id: your_webhook_id
    name: "My Phone"
    icon: mdi:cellphone
```

### Solution Comparison

| Feature | TRSDM Dynamic Device Tracker | Native Webhook Integration |
|---------|------------------------------|----------------------------|
| **Installation Difficulty** | ⭐⭐⭐ Requires HACS | ⭐⭐ Native Support |
| **Configuration Flexibility** | ⭐⭐⭐⭐⭐ Highly Customizable | ⭐⭐ Basic Features |
| **Attribute Management** | ⭐⭐⭐⭐⭐ Dynamic Attributes | ⭐⭐ Fixed Attributes |
| **Device Count** | ⭐⭐⭐⭐⭐ Unlimited | ⭐⭐⭐ Limited |
| **Interface Friendly** | ⭐⭐⭐⭐⭐ GUI | ⭐⭐ Code Configuration |
| **Maintenance Convenience** | ⭐⭐⭐⭐⭐ Easy Management | ⭐⭐ Manual Maintenance |

### Recommended Usage Scenarios

#### TRSDM Dynamic Device Tracker Suitable For:
- 🏠 **Home Users**: Need GUI configuration
- 🔧 **Developers**: Need flexible attribute and device management
- 📱 **Multiple Devices**: Need to track multiple devices
- 🎯 **Customization**: Need custom attributes and features

#### Native Webhook Integration Suitable For:
- 🚀 **Quick Deployment**: Simple one-to-one device tracking
- 📚 **Learning Purpose**: Understand Home Assistant basic features
- 🔒 **Security Considerations**: Don't want to install third-party plugins

### Development Environment
- Android Studio 4.0+
- Android SDK API 14+
- Gradle 6.0+

### Code Standards
- Follow Android development standards
- Add appropriate comments
- Conduct thorough testing

## 📦 Packaging and Installation

### 🔧 Packaging Methods

#### Using Build Scripts (Recommended)
```bash
# Linux/Mac
chmod +x build_scripts.sh
./build_scripts.sh

# Windows
build_scripts.bat
```

#### Manual Packaging
```bash
# Package Release version
./gradlew assembleRelease

# Package Debug version  
./gradlew assembleDebug
```

**Important**: Before manual packaging, you need to configure the `local.properties` file:

1. **Copy Configuration Template**:
```bash
cp local.properties.example local.properties
```

2. **Edit local.properties file**:
```properties
# Signing Configuration (Required for Release version)
KEYSTORE_PASSWORD=your_signing_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password

# Server Configuration (Optional, has default values)
CONFIG_URL=your_config_server_address
HEARTBEAT_URL=your_heartbeat_address
WEBHOOK_URL=your_webhook_address
```

3. **Configuration Description**:
- **Signing Configuration**: Required for Release version packaging, used for APK signing
- **Server Configuration**: Optional, if not configured will use default values
- **Default Values**: 
  - `CONFIG_URL`: `https://www.zhangsan.com/locationtracker/api/config`
  - `HEARTBEAT_URL`: `https://www.zhangsan.com/locationtracker/api/config/heartbeat`
  - `WEBHOOK_URL`: `https://www.zhangsan.com/api/webhook/db72ebc1627e52685ca64cdb380`

### Version Description

#### Release Version
- **Package Name**: `com.hx.locationtracker`
- **Features**: Signed, can be released directly
- **Optimization**: Code obfuscation, resource compression, performance optimization
- **Purpose**: Official release version

#### Debug Version  
- **Package Name**: `com.hx.locationtracker.debug`
- **Features**: Unsigned, for testing
- **Optimization**: Retain debug information, convenient for troubleshooting
- **Purpose**: Development testing version

#### Simultaneous Installation
- Both versions can be installed simultaneously, different package names
- Release version for official use
- Debug version for testing and debugging

### 📥 Installation Methods

#### Method 1: Direct Installation
1. Download APK file from release
2. Install on Android device
3. Grant necessary permissions
4. Configure Webhook URL and reporting interval

#### Method 2: Source Code Compilation
1. Clone project code
2. Open project with Android Studio
3. Compile to generate APK
4. Install on device

## 🚀 Future Development Plan

### 📱 Device-side Feature Enhancement

#### 🔄 Server Configuration Push (Basic Architecture Already Available)
- **Remote Configuration Updates**: Based on existing ConfigSyncService, implement server-side remote push configuration information to device APP
- **Real-time Configuration Sync**: Device automatically receives and applies server-pushed configuration
- **Configuration Version Management**: Support configuration version control and rollback mechanisms
- **Incremental Configuration Updates**: Only push changed configuration items, reduce data transmission

#### 💓 Heartbeat Monitoring System (Basic Architecture Already Available)
- **Device Online Status**: Based on existing HEARTBEAT_URL, monitor device online/offline status in real-time through heartbeat packets
- **Network Status Detection**: Automatically detect network connection quality and stability
- **Offline Time Statistics**: Record device offline duration and frequency
- **Abnormal Status Alerts**: Automatically send alerts when devices go offline abnormally

#### 📊 Status Feedback Mechanism
- **Device Status Reporting**: Regularly report device running status and health information
- **Configuration Application Feedback**: Feedback configuration reception and application status
- **Error Log Upload**: Automatically upload error logs to server
- **Performance Metrics Collection**: Collect device performance metrics for optimization

### 🌐 Server-side Feature Planning

#### 🔧 Device Management Platform
- **Device Registration Management**: Device registration, authentication and permission management
- **Group Management**: Support device grouping and batch operations
- **Configuration Templates**: Predefined configuration templates, support batch configuration push
- **Device Monitoring**: Real-time monitor all device status and location information

#### 📈 Data Statistical Analysis
- **Device Activity Statistics**: Statistics on device online time and activity
- **Configuration Application Statistics**: Statistics on configuration push success rate and application
- **Performance Analysis**: Analyze device performance metrics and optimization suggestions
- **Usage Trends**: Analyze device usage trends and patterns

#### 🔔 Alert Notification System
- **Device Offline Alerts**: Send notifications when devices go offline abnormally
- **Configuration Push Failures**: Send alerts when configuration push fails
- **Abnormal Behavior Detection**: Detect device abnormal behavior and alert
- **System Status Monitoring**: Monitor overall system running status

### 📋 Technical Architecture Design

#### 🔐 Security Mechanisms
- **Device Authentication**: Token-based device identity authentication
- **Data Encryption**: All communication data encrypted with TLS
- **Permission Control**: Fine-grained device permission management
- **Audit Logging**: Complete operation audit log records

#### ⚡ Performance Optimization
- **Connection Pool Management**: Efficient connection pool management mechanism
- **Message Queue**: Reliable message delivery and processing
- **Cache Mechanism**: Smart cache to reduce duplicate requests
- **Load Balancing**: Support multi-server load balancing

#### 🔄 Protocol Design
- **Heartbeat Protocol**: Lightweight heartbeat packet protocol design
- **Configuration Push Protocol**: Standardized configuration push protocol
- **Status Feedback Protocol**: Device status feedback protocol specification
- **Error Handling Protocol**: Unified error handling and retry mechanism

### 📅 Development Timeline

#### Phase 1: Basic Features (1-2 months)
- [ ] Complete ConfigSyncService startup and call mechanism
- [ ] Implement server-side device status monitoring
- [ ] Complete configuration push functionality
- [ ] Implement device authentication mechanism

#### Phase 2: Management Platform (2-3 months)
- [ ] Web management interface development
- [ ] Device grouping management functionality
- [ ] Configuration template system
- [ ] Real-time monitoring panel

#### Phase 3: Advanced Features (3-4 months)
- [ ] Data statistical analysis
- [ ] Alert notification system
- [ ] Performance optimization
- [ ] Security hardening

### 🎯 Feature Comparison

| Feature Module | Current Version | Future Version |
|----------------|-----------------|----------------|
| **Configuration Management** | Local Configuration | Remote Push Configuration |
| **Device Monitoring** | Basic Status | Real-time Heartbeat Monitoring |
| **Data Statistics** | Basic Logs | Detailed Statistical Analysis |
| **Alert Notifications** | Local Notifications | Remote Alert System |
| **Permission Management** | Basic Permissions | Fine-grained Permissions |
| **Security Mechanisms** | Basic Encryption | Enterprise-level Security |

### 💡 Development Principles

#### ✅ Key Development Focus
- **Lightweight Design**: Keep app lightweight, don't affect device performance
- **Open Source Friendly**: All features remain open source, convenient for community contribution
- **User-oriented**: Prioritize features most needed by users
- **Progressive Development**: Develop in phases, ensure each phase has usable version

#### 🔧 Technical Foundation
- **Existing Architecture**: Based on existing ConfigSyncService and heartbeat mechanism
- **Compatibility**: Maintain complete compatibility with existing features
- **Scalability**: Design architecture supporting future feature expansion
- **Stability**: Ensure new features don't affect existing feature stability

## 🤝 Contributing Guidelines

Welcome to submit Issues and Pull Requests to improve the project!

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for details.

## 📞 Contact

If you have questions or suggestions, please contact through:
- Submit GitHub Issues
- Send email to project maintainer

---

**Note**: When using this app, please comply with local laws and regulations, ensure location services are used within legal scope.

## 📝 Changelog

### V2.1.5
- 🧹 **Code Cleanup**: Removed all MQTT-related code and configurations
- 📱 **Version Update**: Upgraded from V2.1.4 to V2.1.5
- 🔧 **Configuration Optimization**: Simplified build configuration, removed unused dependencies
- 📝 **Documentation Update**: Updated version number and feature descriptions
- 🛠️ **ProGuard Optimization**: Updated package name reference rules
- Comprehensive UI detail optimization, improved user experience:
  - Webhook/cycle input boxes added detailed hints, explaining format and range
  - Button debounce processing, prevent multiple "Start" clicks causing duplicate startup
  - All input and operation errors have clear Toast prompts
  - Service start/stop, report success/failure operations have clear UI feedback
  - Night mode/high contrast adaptation, ensure interface clarity under different themes
  - Multi-resolution adaptation (sw400dp, sw600dp, sw720dp, sw800dp, sw900dp), all optimizations synchronized to various screen layouts
- Global exception capture, prevent app crashes and locally save crash logs, friendly prompts when exceptions occur
- Webhook URL, reporting cycle and other input validation more strict, prevent invalid configuration
- Other detail experience optimizations and bug fixes

### Cleanup Details
- ❌ Removed MQTT_USERNAME configuration
- ❌ Removed MQTT_PASSWORD configuration
- ❌ Removed CLIENT_ID configuration
- ❌ Removed TOPIC1 variable
- ❌ Removed clientid, userName, passWord variables
- ❌ Removed MQTT-related string resources
- ✅ Updated ProGuard rules package name references

### V2.1.4
- ✨ **Transparent Status Bar Feature**: Implement immersive status bar effect, status bar above title becomes transparent
- ✨ **Smart Compatibility**: Different Android versions automatically use corresponding transparent schemes
  - Android 5.0+ (API 21+): Fully transparent status bar
  - Android 4.4-4.4W (API 19-20): Semi-transparent status bar
  - Android 4.0-4.3 (API 14-18): Automatically ignore, keep original
- 🛠️ **Technical Implementation**: Layout adaptation and code implementation, ensure backward compatibility
- 🛠️ **Exception Handling**: Transparent status bar setting failure doesn't affect normal app operation

### v2.1.3
- 🚨 **Important Fixes and Optimizations**: Thoroughly fixed theme compatibility crash issues, ensure app runs stably on all devices
- 🛠️ **Smart Theme Setting Mechanism**: Automatically select optimal AppCompat theme based on system status (power save mode, night mode, high contrast, etc.)
- 🛠️ **Multiple Protection Mechanisms**: Set compatible themes in Application, Activity's attachBaseContext and onCreate stages, ensure 100% no crashes
- 🛠️ **Theme Verification Mechanism**: Verify if theme takes effect after each theme setting, automatic fallback when failed
- ✨ **Crash Log Management Optimization**: Fixed issue where dialog information doesn't update after crash log cleanup, provide immediate cleanup function
- ✨ **Screen Status Adaptive Location Interval**: Automatically shorten location interval when screen is off, increase probability of being woken by system
- ✨ **Data Deduplication Mechanism**: Only report when location data changes, reduce invalid reports
- ✨ **Low Battery Smart Protection**: Pause location reporting when battery below 10%, automatically restart when battery recovers
- ✨ **Enhanced WakeLock Mechanism and Keep-alive Strategy**: Use stronger WakeLock strategy to ensure background running
- ✨ **Service Auto-restart Mechanism**: Auto-restart through broadcast mechanism after being killed by system
- ✨ **Boot Auto-start Function**: Automatically start location reporting service after system boot completes
- ✨ **Keep-alive Timer and Status Check**: Random 60-second to configured reporting interval keep-alive check mechanism
- ✨ **Battery Status Monitoring and Power Recovery Logic**: Real-time monitor battery level and power save mode status
- 🔧 **Fixed API Level Compatibility Issues**: Ensure stable operation on all Android versions
- 🔧 **Improved Network Requests and Error Handling**: More comprehensive exception capture and error recovery mechanisms
- 🔧 **Optimized Status Broadcasting and Log System**: Improved status update and log broadcasting mechanisms
- 🔒 **Enhanced Permission Checking and Input Validation**: Stricter permission checking and input validation
- 📱 **Improved User Experience and Interface Display**: Optimized user interface and interaction experience

### v2.1.2
- 🚨 **Important Fix**: Fixed issue where reporting interval automatically adjusts when screen status switches
- ✨ **Data Deduplication Mechanism**: Only report when location data changes
- 🔧 **API Compatibility**: Fixed API level compatibility issues
- 🔧 **Network Optimization**: Improved network requests and error handling
- 🔧 **Log System**: Optimized status broadcasting and log system
- 🔒 **Security Enhancement**: Enhanced permission checking and input validation
- 📱 **Interface Optimization**: Improved user experience and interface display

### v2.1.1
- ✨ Added low battery smart protection feature
- ✨ Enhanced WakeLock mechanism and keep-alive strategy
- ✨ Improved service auto-restart mechanism
- ✨ Completed boot auto-start functionality
- ✨ Added keep-alive timer and status check
- ✨ Optimized battery status monitoring and power recovery logic
- 🔧 Fixed API level compatibility issues
- 🔧 Improved network requests and error handling
- 🔧 Optimized status broadcasting and log system
- 🔒 Enhanced permission checking and input validation
- 📱 Improved user experience and interface display
- ⚠️ **Note**: This version had issue where reporting interval automatically adjusts when screen status switches, fixed in v2.1.2

### v2.0.0
- ✨ Added low battery protection feature
- ✨ Enhanced WakeLock mechanism
- ✨ Optimized screen status monitoring
- ✨ Improved network retry mechanism
- ✨ Completed log system
- 🔒 Fixed SQL injection vulnerability
- 🔒 Enhanced input validation
- 🔒 Improved error handling

### v1.0.0
- 🎉 Initial version release
- 📍 Basic location reporting functionality
- 🌐 HTTP Webhook support
- 📱 Android 4.0+ compatibility