#!/bin/bash

# LocationTracker 服务器部署脚本
# 使用方法: ./deploy.sh [服务器地址] [用户名] [目标路径]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查参数
if [ $# -lt 3 ]; then
    echo -e "${RED}错误: 参数不足${NC}"
    echo "使用方法: $0 [服务器地址] [用户名] [目标路径]"
    echo "示例: $0 your-server.com root /var/www/html/locationtracker"
    exit 1
fi

SERVER_ADDRESS=$1
USERNAME=$2
TARGET_PATH=$3

echo -e "${BLUE}🚀 开始部署 LocationTracker 配置服务器...${NC}"
echo -e "${YELLOW}服务器地址: ${SERVER_ADDRESS}${NC}"
echo -e "${YELLOW}用户名: ${USERNAME}${NC}"
echo -e "${YELLOW}目标路径: ${TARGET_PATH}${NC}"

# 创建临时目录
TEMP_DIR=$(mktemp -d)
echo -e "${BLUE}📁 创建临时目录: ${TEMP_DIR}${NC}"

# 复制文件到临时目录
echo -e "${BLUE}📋 准备部署文件...${NC}"
cp config_api.php "$TEMP_DIR/"
cp index.html "$TEMP_DIR/"

# 创建默认设备配置文件
cat > "$TEMP_DIR/locationtracker_devices.json" << EOF
{}
EOF

# 创建.htaccess文件（Apache）
cat > "$TEMP_DIR/.htaccess" << EOF
RewriteEngine On

# API路由
RewriteRule ^api/config/?$ config_api.php [L,QSA]

# 其他请求转到index.html
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^(.*)$ index.html [L]

# 设置CORS头
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Methods "GET, POST, OPTIONS"
Header always set Access-Control-Allow-Headers "Content-Type"

# 设置安全头
Header always set X-Content-Type-Options nosniff
Header always set X-Frame-Options DENY
Header always set X-XSS-Protection "1; mode=block"

# 设置缓存
<FilesMatch "\.(html|css|js)$">
    Header set Cache-Control "max-age=3600"
</FilesMatch>

<FilesMatch "\.(json|php)$">
    Header set Cache-Control "no-cache, no-store, must-revalidate"
    Header set Pragma "no-cache"
    Header set Expires "0"
</FilesMatch>
EOF

# 创建nginx配置（如果使用nginx）
cat > "$TEMP_DIR/nginx.conf" << EOF
server {
    listen 80;
    server_name your-server.com;
    root /var/www/html/locationtracker;
    index index.html;

    # API路由
    location /api/config {
        try_files \$uri \$uri/ /config_api.php?\$args;
    }

    # 其他请求
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # 安全头
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "1; mode=block";
    add_header Access-Control-Allow-Origin "*";
    add_header Access-Control-Allow-Methods "GET, POST, OPTIONS";
    add_header Access-Control-Allow-Headers "Content-Type";

    # PHP处理
    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php7.4-fpm.sock;
        fastcgi_index index.php;
        fastcgi_param SCRIPT_FILENAME \$document_root\$fastcgi_script_name;
        include fastcgi_params;
    }
}
EOF

# 创建安装说明
cat > "$TEMP_DIR/README.md" << EOF
# LocationTracker 配置服务器

## 部署说明

### 1. 上传文件
所有文件已上传到服务器，位于: \${TARGET_PATH}

### 2. 设置权限
\`\`\`bash
chmod 755 \${TARGET_PATH}
chmod 644 \${TARGET_PATH}/*.php
chmod 644 \${TARGET_PATH}/*.html
chmod 644 \${TARGET_PATH}/*.json
chmod 644 \${TARGET_PATH}/.htaccess
\`\`\`

### 3. 配置Web服务器

#### Apache配置
确保启用了mod_rewrite和mod_headers模块：
\`\`\`bash
a2enmod rewrite
a2enmod headers
systemctl restart apache2
\`\`\`

#### Nginx配置
使用提供的nginx.conf配置文件，或将其内容添加到你的nginx配置中。

### 4. 测试访问
- 管理界面: https://your-server.com/
- API接口: https://your-server.com/api/config

### 5. 更新Android应用
在ConfigSyncService.java中更新CONFIG_URL为你的服务器地址。

## 文件说明
- \`index.html\`: Web管理界面
- \`config_api.php\`: API接口
- \`locationtracker_config.json\`: 配置文件
- \`.htaccess\`: Apache重写规则
- \`nginx.conf\`: Nginx配置示例

## 安全建议
1. 启用HTTPS
2. 设置防火墙规则
3. 定期备份配置文件
4. 监控访问日志
EOF

echo -e "${BLUE}📤 上传文件到服务器...${NC}"

# 使用scp上传文件
if scp -r "$TEMP_DIR"/* "$USERNAME@$SERVER_ADDRESS:$TARGET_PATH/"; then
    echo -e "${GREEN}✅ 文件上传成功！${NC}"
else
    echo -e "${RED}❌ 文件上传失败！${NC}"
    exit 1
fi

# 设置文件权限
echo -e "${BLUE}🔧 设置文件权限...${NC}"
ssh "$USERNAME@$SERVER_ADDRESS" "chmod 755 $TARGET_PATH && chmod 644 $TARGET_PATH/*.php $TARGET_PATH/*.html $TARGET_PATH/*.json $TARGET_PATH/.htaccess"

# 清理临时目录
rm -rf "$TEMP_DIR"

echo -e "${GREEN}🎉 部署完成！${NC}"
echo -e "${BLUE}📋 下一步操作:${NC}"
echo -e "${YELLOW}1. 访问管理界面: https://$SERVER_ADDRESS/${NC}"
echo -e "${YELLOW}2. 测试API接口: https://$SERVER_ADDRESS/api/config${NC}"
echo -e "${YELLOW}3. 更新Android应用中的CONFIG_URL${NC}"
echo -e "${YELLOW}4. 检查服务器日志确保一切正常${NC}"

echo -e "${BLUE}📖 详细说明请查看: $TARGET_PATH/README.md${NC}" 