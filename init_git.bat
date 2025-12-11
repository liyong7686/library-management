@echo off
chcp 65001 >nul
echo ========================================
echo Git 仓库初始化脚本
echo ========================================
echo.

cd /d %~dp0

set REPO_URL=https://github.com/liyong7686/library-management.git

echo 仓库地址: %REPO_URL%
echo.

echo 正在检查 Git 是否已初始化...
if exist .git (
    echo Git 仓库已存在，跳过初始化步骤
) else (
    echo 正在初始化 Git 仓库...
    git init
)

echo.
echo 正在检查 Git 用户配置...
git config user.name >nul 2>&1
if errorlevel 1 (
    echo ⚠️  未配置 Git 用户信息，请先配置：
    echo.
    set /p GIT_NAME="请输入您的姓名: "
    set /p GIT_EMAIL="请输入您的邮箱: "
    git config user.name "%GIT_NAME%"
    git config user.email "%GIT_EMAIL%"
    echo ✅ Git 用户信息已配置
) else (
    echo ✅ Git 用户信息已配置
    git config user.name
    git config user.email
)

echo.
echo 正在添加所有文件...
git add .

echo.
echo 正在提交代码...
git commit -m "Initial commit: 图书管理系统"

echo.
echo 正在检查远程仓库配置...
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo 正在添加远程仓库...
    git remote add origin %REPO_URL%
) else (
    echo 远程仓库已存在，正在更新地址...
    git remote set-url origin %REPO_URL%
)

echo.
echo 正在推送到 GitHub...
git branch -M main 2>nul
git push -u origin main

if errorlevel 1 (
    echo.
    echo ⚠️  推送失败！可能的原因：
    echo 1. 需要 GitHub 认证（Personal Access Token 或 SSH 密钥）
    echo 2. 仓库权限问题
    echo.
    echo 解决方案：
    echo 1. 如果使用 HTTPS，需要 Personal Access Token：
    echo    - 访问 https://github.com/settings/tokens
    echo    - 创建新 token，选择 repo 权限
    echo    - 推送时使用 token 作为密码
    echo.
    echo 2. 如果使用 SSH，需要配置 SSH 密钥：
    echo    - 生成 SSH 密钥：ssh-keygen -t ed25519 -C "your_email@example.com"
    echo    - 添加到 GitHub：https://github.com/settings/keys
    echo    - 修改远程地址：git remote set-url origin git@github.com:liyong7686/library-management.git
    echo.
) else (
    echo.
    echo ========================================
    echo ✅ Git 仓库初始化完成！
    echo ========================================
    echo.
    echo 代码已成功推送到 GitHub：
    echo %REPO_URL%
    echo.
)

pause

