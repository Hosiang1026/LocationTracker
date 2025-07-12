# LocationTracker Release 版本签名信息

## 签名配置

### Keystore 信息
- **Keystore 文件**: `app/keystore/release_v2.keystore`
- **Keystore 密码**: `locationtracker2024`
- **密钥别名**: `locationtracker`
- **密钥密码**: `locationtracker2024`

### 证书信息
- **证书主题**: `CN=LocationTracker, OU=Development, O=LocationTracker, L=City, ST=State, C=CN`
- **证书有效期**: 2025-06-28 至 2052-11-13 (约27年)
- **密钥算法**: RSA 2048位
- **签名算法**: SHA1withRSA

## APK 信息

### Release APK
- **文件路径**: `app/build/outputs/apk/release/app-release.apk`
- **文件大小**: 1,955,030 字节 (约1.9MB)
- **构建时间**: 2025-06-28 23:28:08
- **版本号**: 1.0.0
- **版本代码**: 1

### 构建配置
- **混淆**: 已启用 (minifyEnabled true)
- **资源压缩**: 已启用 (shrinkResources true)
- **签名**: 已配置并应用

## 验证签名

使用以下命令验证APK签名：
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

## 注意事项

1. **自签名证书**: 这是自签名证书，在Google Play Store发布时需要替换为正式的发布证书
2. **证书有效期**: 证书有效期为27年，足够长期使用
3. **安全性**: 请妥善保管keystore文件和密码，丢失将无法更新应用
4. **备份**: 建议将keystore文件备份到安全位置

## 发布说明

此APK已通过签名验证，可以用于：
- 内部测试分发
- 第三方应用商店发布
- 直接安装到设备

如需发布到Google Play Store，请使用Google Play Console生成的正式发布证书。

---

APP版本：V2.1.1  开发者：狂欢马克思 