<p align="right">
  <b>Chinese</b> | <a href="./README.en.md">English</a>
</p>

<br>

# LocationTracker

[![Android Version](https://img.shields.io/badge/Android-4.0+-green.svg)](https://developer.android.com/about/versions/android-4.0)
[![Version](https://img.shields.io/badge/Version-V2.1.6-blue.svg)](https://github.com/Hosiang1026/LocationTracker/releases)
[![Phone UI](https://img.shields.io/badge/Phone%20UI-Single%20Panel-brightgreen.svg)](https://haoxiang.eu.org/ui/MOBILE_UI_PREVIEW)
[![Tablet UI](https://img.shields.io/badge/Tablet%20UI-Optimized%20for%20Large%20Screen-blue.svg)](https://haoxiang.eu.org/ui/TABLET_UI_PREVIEW)

> 📱 **Smart Location Reporting App** - Send Android device location data to Home Assistant or other servers via HTTP Webhook

## 🚨 Important Notice

> 🚨 **Anti-fraud Statement**: This app is fully open source and free. Do not trust any paid versions or services!
> 
> - 📱 **Official Channel**: Download only from the official GitHub repository
> - 💰 **Completely Free**: All features are free, no paid items
> - 🔒 **Open Source**: Code is fully open, free to view and modify
> - ⚠️ **Beware of Scams**: If you encounter any payment requests, report and block immediately
> - 📞 **Official Support**: For issues, use GitHub Issues

---

## 🚀 Quick Start

### 📥 Installation
1. Download the [APK file](./app/build/outputs/apk/release/)
2. Install on your Android device
3. Grant necessary permissions
4. Configure Webhook URL (must be http/https, validated, not empty)
5. Set reporting interval (10~10800 seconds, not empty, validated)
6. Tap "Start Location" to begin

### ⚡ Core Features
- 📍 **Real-time Location**: GPS/network location, accurate reporting
- 🔄 **Data Deduplication**: Avoid duplicate location reports
- 🔋 **Low Battery Protection**: Auto-pause reporting below 10% battery
- 🌙 **Background Keep-alive**: Auto-start on boot, service auto-restart
- 🎨 **Transparent Status Bar**: Android 4.4+ supported
- 🛡️ **Global Exception Capture**: Crashes are logged and user is notified
- 📝 **Input Validation**: Strict checks for Webhook URL and interval
- 🖥️ **Multi-resolution Support**: Adapts to different screen sizes
- 🔧 **Device Optimization Guide**: Built-in brand-specific optimization tips
- 🔒 **SQL Injection Protection**: Parameterized queries for DB
- 🖱️ **Main operations have UI feedback**

## 🎨 UI Preview

> 💡 **Quick Preview**: [📱 Phone UI](https://haoxiang.eu.org/ui/MOBILE_UI_PREVIEW) | [📟 Tablet UI](https://haoxiang.eu.org/ui//TABLET_UI_PREVIEW)

### 📱 Phone Features
- **Single panel design**: Bottom TAB switch, clean UI
- **Status Monitor**: Real-time connection, location, battery, report count
- **Config Panel**: Webhook URL, interval, notification switch
- **Log Panel**: Real-time app status and report logs

### 📟 Tablet Features
- **Large screen optimized**: Layout and font size for tablets
- **Touch friendly**: Larger buttons and controls
- **Info density**: Efficient use of large screens
- **Portrait/Landscape**: Good UX in both orientations

## 🚀 User Guide

### First Use
1. **Install the app**: Download and install the APK file
2. **Grant permissions**: Allow location, network, auto-start, etc.
3. **Configure parameters**: Set Webhook URL and reporting interval in the config panel
4. **Start service**: Tap "Start Location" to begin reporting
5. **Monitor status**: Switch to the monitor panel to view status and logs

### UI Operations
- **📊 Monitor Panel**: View connection, location, battery, report count
- **⚙️ Config Panel**: Set Webhook URL, interval, notification switch
- **📱 Bottom Navigation**: Switch between monitor and config panels
- **🛠️ Log Management**: Tap "🛠️ Log" to view crash logs
- **🔧 Optimization Settings**: Tap "Optimization Settings" for device-specific tips

### Daily Use
- **Auto-start**: The app will auto-start the location reporting service
- **Background running**: Service runs in the background, no manual intervention needed
- **Status monitoring**: View connection and report count in the status panel
- **Log viewing**: Check detailed logs for troubleshooting
- **Notification monitoring**: See current report time and status in the notification bar

### Troubleshooting
1. **Service won't start**: Check if GPS is on and permissions are granted
2. **Data upload fails**: Check network connection and Webhook URL
3. **Service killed in background**: Check device optimization settings
4. **High battery usage**: Increase reporting interval or enable low battery protection
5. **Theme compatibility issues**: The app has built-in theme adaptation; check crash logs if issues persist

## 📋 Permissions

### Required Permissions
- `ACCESS_FINE_LOCATION`: Precise location
- `ACCESS_COARSE_LOCATION`: Approximate location
- `ACCESS_BACKGROUND_LOCATION`: Background location (Android 10+)
- `INTERNET`: Network access
- `ACCESS_NETWORK_STATE`: Network state
- `WAKE_LOCK`: Wake lock
- `RECEIVE_BOOT_COMPLETED`: Auto-start on boot

### Optional Permissions
- `FOREGROUND_SERVICE`: Foreground service (Android 8.0+)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Ignore battery optimizations
- `SYSTEM_ALERT_WINDOW`: Overlay (Huawei/Honor)
- `ACCESS_WIFI_STATE`: WiFi state (for network location)
- `CHANGE_WIFI_STATE`: Change WiFi state
- `ACCESS_LOCATION_EXTRA_COMMANDS`: Extra location commands
- `READ_EXTERNAL_STORAGE`: Read external storage (for crash logs)
- `WRITE_EXTERNAL_STORAGE`: Write external storage (for crash logs)
- `READ_PHONE_STATE`: Read phone state
- `WRITE_SETTINGS`: Write system settings
- `BLUETOOTH`: Bluetooth
- `BLUETOOTH_ADMIN`: Bluetooth admin

## 🔒 Security Analysis

### Fixed Security Issues
✅ **SQL Injection Protection**: All DB operations use parameterized queries
✅ **Network Security**: Uses OkHttp, supports HTTPS, with timeout and retry
✅ **Permission Management**: Complete runtime permission requests
✅ **Input Validation**: Strict user input validation
✅ **API Compatibility**: Fixed all API level compatibility issues

### Security Recommendations
- 🔐 Use HTTPS Webhook URLs
- 🔐 Update the app regularly
- 🔐 Use in trusted networks
- 🔐 Regularly check reported data accuracy

## ⚙️ Configuration

### Basic Configuration
1. **Webhook URL**: Must start with http/https, not empty, validated
2. **Reporting interval**: 10~10800 seconds, not empty, validated
3. **Notification switch**: Controls foreground notification, keeps app running in background

### Data Format

The app sends different data formats depending on the reporting scenario:

#### Standard Location Report
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

#### Immediate Report (with extra flag)
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

#### WorkManager Simple Format
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "altitude": 100.5,
  "accuracy": 5.0,
  "timestamp": 1640995200000
}
```

### Field Descriptions

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `latitude` | number | Latitude | ✅ |
| `longitude` | number | Longitude | ✅ |
| `altitude` | number | Altitude (meters) | ⚠️ |
| `gps_accuracy` / `accuracy` | number | GPS accuracy (meters) | ⚠️ |
| `battery` | number | Battery percentage | ⚠️ |
| `speed` | number | Speed (m/s) | ⚠️ |
| `bearing` | number | Bearing (0-360°) | ⚠️ |
| `timestamp` | number | Timestamp (ms) | ✅ |
| `provider` | string | Location provider (gps/network/passive) | ⚠️ |
| `screen_off` | boolean | Screen off | ⚠️ |
| `power_save_mode` | boolean | Power save mode | ⚠️ |
| `immediate_report` | boolean | Immediate report | ⚠️ |

**Note**: Only `latitude` and `longitude` are always required; other fields may be missing depending on device and system state.

## 📊 Compatibility

### 🖥️ Screen Sizes
| Device Type | Screen Size | Resolution | Layout | Touch Optimized | Status Bar | Example Devices |
|-------------|------------|------------|--------|-----------------|------------|----------------|
| Small Phone | 4.0-4.7"   | 480x800-720x1280 | ✅ Compact | ✅ Standard | ✅ Transparent | Xiaomi 4, Huawei P8, Samsung S5 |
| Standard Phone | 5.0-6.0" | 720x1280-1080x1920 | ✅ Single panel | ✅ Standard | ✅ Transparent | Huawei P40, Xiaomi 12, OPPO Find X3 |
| Large Phone | 6.1-6.7"   | 1080x1920-1440x3200 | ✅ Single panel | ✅ Standard | ✅ Transparent | Huawei Mate 50 Pro, Xiaomi 13 Ultra |
| Small Tablet | 7.0-8.0"  | 1024x768-1200x1920 | ✅ Optimized | ✅ Large buttons | ✅ Transparent | Huawei M6, Xiaomi Pad 4 |
| Standard Tablet | 8.5-10.5" | 1200x1920-1600x2560 | ✅ Optimized | ✅ Large buttons | ✅ Transparent | Huawei MatePad Pro, Xiaomi Pad 5 |
| Large Tablet | 11.0-12.9" | 1600x2560-2048x2732 | ✅ Large screen | ✅ Extra large buttons | ✅ Transparent | Huawei MatePad Pro 12.6, Xiaomi Pad 6 Pro |
| Foldable | 6.7-8.0" unfolded | 1080x1920-2208x1768 | ✅ Adaptive | ✅ Smart size | ✅ Transparent | Samsung Fold, Huawei Mate X |
| Car/Big Screen | 10.0-15.0" | 1920x1080-2560x1440 | ✅ Large screen | ✅ Extra large buttons | ✅ Transparent | Android Car, Smart TV |

### 🤖 Android Versions
| Feature | 4.0-4.3 | 4.4-4.4W | 5.0+ | 10+ |
|---------|---------|----------|------|-----|
| Basic Location | ✅ | ✅ | ✅ | ✅ |
| Background Location | ✅ | ✅ | ✅ | ⚠️ Permission needed |
| Foreground Service | ❌ | ❌ | ✅ | ✅ |
| Transparent Status Bar | ❌ | ⚠️ Semi | ✅ | ✅ |
| Theme Compatibility | ✅ | ✅ | ✅ | ✅ |

### 📱 Device Brands
| Brand | Auto-start | Background Keep-alive | Battery Optimization | Optimization Guide |
|-------|------------|----------------------|---------------------|-------------------|
| Huawei/Honor | ⚠️ Needs setting | ⚠️ Needs optimization | ⚠️ Needs ignore | ✅ Built-in |
| Xiaomi/Redmi | ⚠️ Needs setting | ⚠️ Needs optimization | ⚠️ Needs ignore | ✅ Built-in |
| OPPO/OnePlus | ⚠️ Needs setting | ⚠️ Needs optimization | ⚠️ Needs ignore | ✅ Built-in |
| vivo | ⚠️ Needs setting | ⚠️ Needs optimization | ⚠️ Needs ignore | ✅ Built-in |
| Samsung | ⚠️ Needs setting | ⚠️ Needs optimization | ⚠️ Needs ignore | ✅ Built-in |

## 🏠 Home Assistant Integration

> 💡 **Recommended**: Use with [TRSDM Dynamic Device Tracker](https://github.com/Dekadinious/trsdm_custom_device_tracker_for_home_assistant) for flexible device and attribute management

### Solution 1: TRSDM Dynamic Device Tracker (Recommended)

#### Installation Steps
1. Make sure [HACS](https://hacs.xyz/) (Home Assistant Community Store) is installed
2. In Home Assistant, go to HACS > Integrations
3. Click the "+" button and search for "TRSDM Dynamic Device Tracker"
4. Click to install the TRSDM Dynamic Device Tracker integration
5. Restart Home Assistant

#### Configuration Steps
1. In Home Assistant, go to Settings > Devices & Services
2. Click the "+" button to add a new integration
3. Search for "TRSDM Dynamic Device Tracker" and select it
4. Follow the prompts to set up your first device tracker:
   - Give the tracker a name (e.g., "My Phone")
   - The integration will automatically generate a unique webhook URL

#### Usage
1. **Configure Webhook URL**: Enter the Home Assistant webhook URL in the app's config panel
2. **Set reporting interval**: Configure the location reporting interval (10-10800 seconds)
3. **Start service**: Tap "Start Location" to start reporting
4. **Auto reporting**: The app will automatically send location data to the webhook URL at the configured interval

**Example config**:
- Webhook URL: `https://your-home-assistant-url/api/webhook/your-webhook-id`
- Reporting interval: `60` seconds
- Notification switch: `On` (keep app running in background)

**Data format**: The app will automatically send JSON data with location info; only `latitude` and `longitude` are always required, other fields are added as available.

> ⚠️ **Important**: The reporting address must use a domain name or public IP, not a local IP (e.g., 192.168.x.x, 10.x.x.x, etc.). Phones need to access Home Assistant via the internet, so local IPs won't work.

#### Standard Attributes
- Distance from home (meters and miles)
- Direction relative to home (towards, away_from, stationary)
- Bearing from home (N, NE, E, SE, S, SW, W, NW)
- Last update timestamp
- Last significant location change (10m accumulated)

### Solution 2: Native Webhook Integration

#### Automation Config
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

#### Device Tracker Config
```yaml
device_tracker:
  - platform: webhook
    webhook_id: your_webhook_id
    name: "My Phone"
    icon: mdi:cellphone
```

### Solution Comparison

| Feature | TRSDM Dynamic Device Tracker | Native Webhook |
|---------|------------------------------|----------------|
| **Install Difficulty** | ⭐⭐⭐ Needs HACS | ⭐⭐ Native |
| **Config Flexibility** | ⭐⭐⭐⭐⭐ Highly customizable | ⭐⭐ Basic |
| **Attribute Management** | ⭐⭐⭐⭐⭐ Dynamic | ⭐⭐ Fixed |
| **Device Count** | ⭐⭐⭐⭐⭐ Unlimited | ⭐⭐⭐ Limited |
| **UI Friendliness** | ⭐⭐⭐⭐⭐ GUI | ⭐⭐ Code config |
| **Maintenance** | ⭐⭐⭐⭐⭐ Easy | ⭐⭐ Manual |

### Recommended Scenarios

#### TRSDM Dynamic Device Tracker is best for:
- 🏠 **Home users**: Need GUI config
- 🔧 **Developers**: Need flexible attribute/device management
- 📱 **Multi-device**: Need to track multiple devices
- 🎯 **Customization**: Need custom attributes/features

#### Native Webhook is best for:
- 🚀 **Quick setup**: Simple one-to-one tracking
- 📚 **Learning**: Understand Home Assistant basics
- 🔒 **Security**: Don't want third-party plugins

### Development Environment
- Android Studio 4.0+
- Android SDK API 14+
- Gradle 6.0+

### Code Standards
- Follows Android development best practices
- Well-commented
- Thoroughly tested

## 📦 Build & Install

### 🔧 Build Methods

#### Using Build Script (Recommended)
```bash
# Linux/Mac
chmod +x build_scripts.sh
./build_scripts.sh

# Windows
build_scripts.bat
```

#### Manual Build
```bash
# Build Release
./gradlew assembleRelease

# Build Debug
./gradlew assembleDebug
```

**Important**: Before building, configure `local.properties`:

1. **Copy template**:
```bash
cp local.properties.example local.properties
```

2. **Edit local.properties**:
```properties
# SDK path
sdk.dir=your local Android SDK path

# Signing config (required for Release)
KEYSTORE_PASSWORD=your keystore password
KEY_ALIAS=your key alias
KEY_PASSWORD=your key password

# Server config (optional, has defaults)
CONFIG_URL=your config server
HEARTBEAT_URL=your heartbeat URL
WEBHOOK_URL=your webhook URL
```

3. **Config notes**:
- **Signing config**: Required for Release build
- **Server config**: Optional, defaults used if not set
- **Defaults**:
  - `CONFIG_URL`: `https://www.zhangsan.com/locationtracker/api/config`
  - `HEARTBEAT_URL`: `https://www.zhangsan.com/locationtracker/api/config/heartbeat`
  - `WEBHOOK_URL`: `https://www.zhangsan.com/api/webhook/db72ebc1627e52685ca64cdb380`

### Version Info

#### Release
- **Package**: `com.hx.locationtracker`
- **Signed**: Yes, ready for release
- **Optimized**: ProGuard, resource compression, performance
- **Use**: Official release

#### Debug
- **Package**: `com.hx.locationtracker.debug`
- **Signed**: No, for testing
- **Optimized**: Debug info kept
- **Use**: Development/testing

#### Dual Install
- Both versions can be installed together (different package names)
- Release for production, Debug for testing

### 📥 Install

#### Method 1: Direct Install
1. Download APK from release
2. Install on Android device
3. Grant necessary permissions
4. Configure Webhook URL and interval

#### Method 2: Build from Source
1. Clone the repo
2. Open in Android Studio
3. Build APK
4. Install on device

## 🚀 Future Plans

### 📱 Device-side Enhancements

#### 🔄 Server-side Config Push (infra ready)
- **Remote config update**: Use ConfigSyncService to push config from server
- **Real-time sync**: Device auto-applies server config
- **Config versioning**: Support version control and rollback
- **Incremental update**: Only push changed items

#### 💓 Heartbeat Monitoring (infra ready)
- **Device online status**: Use HEARTBEAT_URL for real-time online/offline
- **Network quality**: Auto-detect network quality
- **Offline stats**: Track offline duration/frequency
- **Alerting**: Auto-alert on abnormal offline

#### 📊 Status Feedback
- **Device status reporting**: Regularly report health
- **Config feedback**: Report config apply status
- **Error log upload**: Auto-upload error logs
- **Performance metrics**: Collect for optimization

### 🌐 Server-side Plans

#### 🔧 Device Management Platform
- **Device registration/auth**
- **Grouping/batch ops**
- **Config templates**
- **Device monitoring**

#### 📈 Data Analytics
- **Device activity stats**
- **Config apply stats**
- **Performance analysis**
- **Usage trends**

#### 🔔 Alert System
- **Device offline alerts**
- **Config push failure alerts**
- **Abnormal behavior detection**
- **System health monitoring**

### 📋 Architecture

#### 🔐 Security
- **Device auth**: Token-based
- **Data encryption**: TLS for all comms
- **Permission control**: Fine-grained
- **Audit logs**: Full operation logs

#### ⚡ Performance
- **Connection pooling**
- **Message queue**
- **Caching**
- **Load balancing**

#### 🔄 Protocols
- **Heartbeat**: Lightweight
- **Config push**: Standardized
- **Status feedback**: Standardized
- **Error handling**: Unified

### 📅 Timeline

#### Phase 1: Core (1-2 months)
- [ ] Improve ConfigSyncService startup/calls
- [ ] Implement server device monitoring
- [ ] Improve config push
- [ ] Device auth

#### Phase 2: Management Platform (2-3 months)
- [ ] Web UI
- [ ] Device grouping
- [ ] Config templates
- [ ] Real-time monitoring

#### Phase 3: Advanced (3-4 months)
- [ ] Data analytics
- [ ] Alert system
- [ ] Performance
- [ ] Security

### 🎯 Feature Comparison

| Module | Current | Future |
|--------|---------|--------|
| **Config** | Local | Remote push |
| **Monitoring** | Basic | Real-time heartbeat |
| **Analytics** | Basic logs | Detailed stats |
| **Alerts** | Local | Remote system |
| **Permissions** | Basic | Fine-grained |
| **Security** | Basic | Enterprise |

### 💡 Principles

#### ✅ Focus
- **Lightweight**
- **Open source**
- **User-driven**
- **Incremental**

#### 🔧 Tech
- **Current infra**: ConfigSyncService, heartbeat
- **Compatibility**
- **Extensibility**
- **Stability**

## 🤝 Contributing

PRs and issues welcome!

## 📄 License

MIT License, see LICENSE.

## 📞 Contact

- GitHub Issues
- Email the maintainer

---

**Note**: Please comply with local laws when using this app.

## 📝 Changelog

### v2.1.6 (2024-07-19)
- Location reporting strategy optimized: during daytime, reporting continues regardless of stationary/moving/screen-off; at night, reporting pauses when stationary
- Improved stationary/moving detection logs: distance, speed, and staticCount are now logged for debugging
- Fixed frequent WorkManager task restarts; stationary/moving state and logs are now consistent
- UI auto-refresh: when switching between foreground/background, the status page now refreshes automatically (onResume actively requests service status)
- Permission logs are only written on first entry or when permission state changes, avoiding log spam
- Permission requests are now triggered only after all guide dialogs are closed on first entry, for better UX
- Config dialog: when clicking "Go to fill", the dialog closes immediately before switching to the config tab
- Guide dialog sequence and interaction optimized (device optimization, permission, config)
- Notification content improved: shows "Locating..." when location is not available, "Getting..." when battery is not available
- Silent notification supported: when notification is disabled in config, Android 8.0+ uses silent notification; enabling notification switches to normal immediately
- Notification content intelligently displays "Getting...", "Locating...", or actual values based on battery/location state
- Other minor UX improvements

### v2.1.5 (archived)
- 🧹 **Code Cleanup**: Removed all MQTT-related code and configurations
- 📱 **Version Update**: Upgraded from V2.1.4 to V2.1.5
- 🔧 **Configuration Optimization**: Simplified build configuration, removed unused dependencies
- 📝 **Documentation Update**: Updated version number and feature descriptions
- 🛠️ **ProGuard Optimization**: Updated package name reference rules
- Comprehensive UI detail optimization for better UX:
  - Added detailed hints for Webhook/interval input fields
  - Debounced button to prevent repeated "Start" clicks
  - Clear UI feedback for service start/stop, report success/failure
  - Multi-resolution adaptation (sw400dp, sw600dp, sw720dp, sw800dp, sw900dp)
- Global exception capture, crash logs saved locally, user-friendly error prompts
- Stricter input validation for Webhook URL, interval, etc.
- Other minor UX improvements and bug fixes