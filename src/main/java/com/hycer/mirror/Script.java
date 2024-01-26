package com.hycer.mirror;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Script {
    public static void writeRollbackFile(File retreatScriptFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(retreatScriptFile));
        if (Constants.SYSTEM_TYPE == 1){
            writer.print("@echo off\n" +
                    "setlocal\n" +
                    "REM 休眠10秒\n" +
                    "echo 等待服务端关闭\n" +
                    "ping 127.0.0.1 -n 11 > nul\n" +
                    "echo shutdown\n"+
                    "ping 127.0.0.1 -n 6 > nul\n" +
                    "REM 获取当前目录的父目录\n" +
                    "for %%I in (\"%~dp0..\") do set \"parentDirectory=%%~fI\"\n" +
                    "REM 删除world目录\n" +
                    "rmdir /s /q \"%parentDirectory%\\world\"\n" +
                    "REM 备份文件路径\n" +
                    "set \"backupFile=%parentDirectory%\\MirrorBackup\\%~1\"\n" +
                    "echo copy:%backupFile%\n" +
                    "REM 复制文件夹\n" +
                    "xcopy /E /I /Y \"%backupFile%\" \"%parentDirectory%\\world\"\n" +
                    "cd /d \"%parentDirectory%\"\n" +
                    "echo 存档替换完成\n" +
                    "REM 启动脚本\n" +
                    "call \"%parentDirectory%\\start.bat\"\n" +
                    "REM 获取脚本的退出代码\n" +
                    "set \"exitCode=%errorlevel%\"\n" +
                    "if %exitCode% equ 0 (\n" +
                    "    echo 服务端已关闭\n" +
                    ")\n" +
                    "endlocal");
        } else {
            writer.print("#!/bin/bash\n" +
                    "# 休眠10秒\n" +
                    "echo \"等待服务端关闭\"\n" +
                    "sleep 10\n" +
                    "\n" +
                    "# 获取当前目录的父目录\n" +
                    "currentDirectory=$(cd $(dirname $0);pwd)\n" +
                    "parentDirectory=$(dirname \"$currentDirectory\")\n" +
                    "echo \"当前路径:$currentDirectory\"\n" +
                    "echo \"父级路径:$parentDirectory\"\n" +
                    "targetBackup=$1\n" +
                    "\n" +
                    "# 检查screen是否安装\n" +
                    "if ! command -v screen >/dev/null 2>&1; then\n" +
                    "    echo \"错误: screen未安装。请先安装screen。\"\n" +
                    "    exit 1\n" +
                    "fi\n" +
                    "\n" +
                    "# 回档操作\n" +
                    "rollback(){\n" +
                    "  # 删除world目录\n" +
                    "  rm -rf \"$parentDirectory/world\"\n" +
                    "  # 备份文件路径\n" +
                    "  backupFile=\"$parentDirectory/MirrorBackup/$targetBackup\"\n" +
                    "  echo \"copy:$backupFile\"\n" +
                    "  # 复制文件夹\n" +
                    "  cp -r \"$backupFile\" \"$parentDirectory/world\"\n" +
                    "  cd \"$parentDirectory\"\n" +
                    "  echo \"存档替换完成\"\n" +
                    "}\n" +
                    "\n" +
                    "echo \"shutdown\"\n" +
                    "sleep 5\n" +
                    "# 检查是否在screen会话中运行\n" +
                    "if [ -z \"$STY\" ]; then\n" +
                    "    echo \"警告: 脚本不在screen会话中运行。\"\n" +
                    "    echo \"回档中...\"\n" +
                    "    rollback\n" +
                    "    cd \"$parentDirectory\"\n" +
                    "    screen -dmS mc_server sh start.sh\n" +
                    "    echo \"服务器已在screen会话 mc_server 中启动。\"\n" +
                    "else\n" +
                    "    # 提取当前screen会话名\n" +
                    "    SCREEN_SESSION_NAME=$(echo \"$STY\" | awk -F'.' '{print $NF}')\n" +
                    "    echo \"当前screen会话名称: $SCREEN_SESSION_NAME\"\n" +
                    "\n" +
                    "    # 执行地图回档操作\n" +
                    "    echo \"回档中...\"\n" +
                    "    rollback\n" +
                    "\n" +
                    "    # 重启Minecraft服务器\n" +
                    "    echo \"在当前screen会话 '$SCREEN_SESSION_NAME' 中重启服务器...\"\n" +
                    "    screen -S \"$SCREEN_SESSION_NAME\" -p 0 -X stuff \"$(printf \\\\r)\"\n" +
                    "    screen -S \"$SCREEN_SESSION_NAME\" -p 0 -X stuff \"cd  \"$parentDirectory\"$(printf \\\\r)\"\n" +
                    "    screen -S \"$SCREEN_SESSION_NAME\" -p 0 -X stuff \"sh start.sh$(printf \\\\r)\"\n" +
                    "    echo \"服务器重启命令已发送。\"\n" +
                    "  fi\n" +
                    "\n");
        }
        writer.close();

    }
}
