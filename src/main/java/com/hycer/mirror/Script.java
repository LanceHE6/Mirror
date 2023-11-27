package com.hycer.mirror;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Script {
    public static void writeRetreatBatFile(File retreatBatFile) throws IOException {
        // 创建 PrintWriter 对象写入 retreat.bat 文件
        PrintWriter writer = new PrintWriter(new FileWriter(retreatBatFile));
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
        writer.close();
    }
}
