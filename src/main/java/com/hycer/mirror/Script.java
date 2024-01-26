package com.hycer.mirror;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Script {
    public static void writeRetreatFile(File retreatScriptFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(retreatScriptFile));
        if (Constants.SYSTEM_TYPE == 1){
            // 创建 PrintWriter 对象写入 retreat.bat 文件
            // 写入 retreat.bat 的内容
            writer.println("@echo off");
            writer.println("setlocal");
            writer.println("REM 休眠5秒");
            writer.println("echo 等待服务端关闭");
            writer.println("ping 127.0.0.1 -n 6 > nul");
            writer.println("REM 获取当前目录的父目录");
            writer.println("for %%I in (\"%~dp0..\") do set \"parentDirectory=%%~fI\"");
            writer.println("REM 删除world目录");
            writer.println("rmdir /s /q \"%parentDirectory%\\world\"");
            writer.println("REM 备份文件路径");
            writer.println("set \"backupFile=%parentDirectory%\\MirrorBackup\\%~1\"");
            writer.println("echo copy:%backupFile%");
            writer.println("REM 复制文件夹");
            writer.println("xcopy /E /I /Y \"%backupFile%\" \"%parentDirectory%\\world\"");
            writer.println("cd /d \"%parentDirectory%\"");
            writer.println("echo 存档替换完成");
            writer.println("REM 启动脚本");
            writer.println("call \"%parentDirectory%\\start.bat\"");
            writer.println("REM 获取脚本的退出代码");
            writer.println("set \"exitCode=%errorlevel%\"");
            writer.println("if %exitCode% equ 0 (");
            writer.println("    echo 服务端已关闭");
            writer.println(")");
            writer.println("endlocal");
            // 关闭 PrintWriter 对象
        } else {
            // 创建 PrintWriter 对象写入 retreat.sh 文件
            // 写入 retreat.sh 的内容
            writer.println("#!/bin/bash");
            writer.println("");
            writer.println("# 休眠5秒");
            writer.println("echo \"等待服务端关闭\"");
            writer.println("sleep 5");
            writer.println("");
            writer.println("# 获取当前目录的父目录");
            writer.println("currentDirectory=$(cd $(dirname $0);pwd)");
            writer.println("parentDirectory=$(dirname $currentDirectory)");
            writer.println("echo $currentDirectory");
            writer.println("echo $parentDirectory");
            writer.println("# 删除world目录");
            writer.println("rm -rf \"$parentDirectory/world\"");
            writer.println("");
            writer.println("# 备份文件路径");
            writer.println("backupFile=\"$parentDirectory/MirrorBackup/$1\"");
            writer.println("echo \"copy:$backupFile\"");
            writer.println("");
            writer.println("# 复制文件夹");
            writer.println("cp -r \"$backupFile\" \"$parentDirectory/world\"");
            writer.println("");
            writer.println("cd \"$parentDirectory\"");
            writer.println("echo \"存档替换完成\"");
            writer.println("");
            writer.println("# 启动脚本");
            writer.println("./start.sh");
            writer.println("");
            writer.println("# 获取脚本的退出代码");
            writer.println("exitCode=$?");
            writer.println("");
            writer.println("if [ $exitCode -eq 0 ]; then");
            writer.println("    echo \"服务端已关闭\"");
            writer.println("fi");
            writer.println("");
            // 关闭 PrintWriter 对象
        }
        writer.close();

    }
}
