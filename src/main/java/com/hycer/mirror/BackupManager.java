package com.hycer.mirror;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager {
    private String backupPath = "backup/";
    private String worldPath = "world";

    BackupManager() {
        // 检查备份路径是否存在
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdir();
            if (created){
                System.out.println("backup directory created successfully");
            } else {
                System.out.println("backup directory creation failed");
            }
        } else {
            System.out.println("backup directory already exists");
        }
    }

    public void backup(ServerCommandSource player) throws IOException {

        player.sendMessage(Text.of("开始地图备份"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        LocalDateTime localDateTime = LocalDateTime.now();
        String nowTime = localDateTime.format(formatter);
        backupPath += nowTime;
        DirClone dirClone = new DirClone(worldPath, backupPath, player);
        dirClone.backup();
        player.sendMessage(Text.of("地图备份完成"));
    }


}
