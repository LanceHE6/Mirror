package com.hycer.mirror;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class BackupManager {
    private String backupPath = "backup/";
    private final String worldPath = "world";

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
        }
    }

    public void backup(CommandContext<ServerCommandSource> context, boolean haveTag) throws IOException, InterruptedException {
        ServerCommandSource source =  context.getSource();
        source.sendMessage(Text.of("§b[Mirror]§6服务器将在 §c10s §6后进行地图备份！"));

        Runnable task = () -> {
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            source.getServer().saveAll(false, false, false);
            source.sendMessage(Text.of("§6游戏数据已保存，开始地图备份"));
            System.out.println("进行服务器备份");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 获取当前日期时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            LocalDateTime localDateTime = LocalDateTime.now();
            String nowTime = localDateTime.format(formatter);
            // 是否有tag判断
            if (haveTag){
                String tag = StringArgumentType.getString(context, "tag");
                if (!tag.isEmpty()){
                    backupPath += tag;
                } else {
                    backupPath += nowTime;
                }
            } else {
                backupPath += nowTime;
            }

            DirClone dirClone = new DirClone(worldPath, backupPath, source);
            try {
                dirClone.backup();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            source.sendMessage(Text.of("§6地图备份完成：" + backupPath));
            System.out.println("备份完成");
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);

        // 关闭线程池
        executor.shutdown();
    }

}
