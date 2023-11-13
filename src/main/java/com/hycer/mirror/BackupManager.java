package com.hycer.mirror;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class BackupManager {
    private String backupPath = "MirrorBackup\\";
    private final String worldPath = "world";

    private final int maxBackups = 5;

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
    public String getBackupDir(){
        return backupPath;
    }
    public void backup(CommandContext<ServerCommandSource> context, boolean haveTag) throws IOException, InterruptedException {
        ServerCommandSource source =  context.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(source.getWorld().getRegistryKey());

        Utils.broadcastToAllPlayers(world, "§b[Mirror]§6服务器将在 §c5s §6后进行地图备份！");
        Runnable task = () -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            checkBackupCount(world); // 检查备份数量
            source.getServer().saveAll(false, false, false);
            Utils.broadcastToAllPlayers(world, "§b[Mirror]§6游戏数据已保存，开始地图备份");
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


            DirClone dirClone = new DirClone(worldPath, backupPath, world);
            try {
                dirClone.backup();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Utils.broadcastToAllPlayers(world, "§b[Mirror]§6地图备份完成：" + backupPath);
            System.out.println("备份完成");
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);

        // 关闭线程池
        executor.shutdown();
    }

    private void checkBackupCount(World world) {
        File backupDir = new File(backupPath);
        File[] backupFiles = backupDir.listFiles();
        if (backupFiles != null && backupFiles.length >= maxBackups) {
            // 根据文件的最后修改时间进行排序
            Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

            // 删除最早的备份
            int numToDelete = backupFiles.length - maxBackups + 1;
            for (int i = 0; i < numToDelete; i++) {
                File fileToDelete = backupFiles[i];
                deleteBackup(fileToDelete);
                Utils.broadcastToAllPlayers(world, "§b[Mirror]§4已删除溢出备份: §6" + fileToDelete.getName());
                System.out.println("已删除溢出备份" + fileToDelete.getName());
            }
        }
    }

    private void deleteBackup(File backupFile) {
        File[] files = backupFile.listFiles();
        //遍历该目录下的文件对象
        if (files != null) {
            for (File f : files) {
                //判断子目录是否存在子目录,如果是文件则删除
                if (f.isDirectory()) {
                    //递归删除目录下的文件
                    deleteBackup(f);
                } else {
                    //文件删除
                    if (!f.delete()){
                        return;
                    }
                }
            }
        }
        //文件夹删除
        if (!backupFile.delete()){
            return;
        }
    }

    public Object[] getBackupList(){
        File backupDir = new File(backupPath);
        // 只获取文件夹
        File[] backupFiles = backupDir.listFiles(File::isDirectory);
        if (backupFiles != null) {
            return Arrays.stream(backupFiles).toArray();
        }
        return new Object[0];
    }

    public void retreat(CommandContext<ServerCommandSource> context, String backupFile) throws IOException, InterruptedException {
        File sourceBackup = new File(backupPath + backupFile);
        if (!sourceBackup.exists() || !sourceBackup.isDirectory()) {
            Utils.broadcastToAllPlayers(context.getSource().getWorld(), "指定的备份文件不存在或不是文件夹");
            System.out.println("指定的备份文件不存在或不是文件夹");
            return;
        }
        Utils.broadcastToAllPlayers(context.getSource().getWorld(), "正在关闭服务器");
        MinecraftServer server = context.getSource().getServer();
        System.out.println(System.getProperty("user.dir"));
        String javaCommand = "java Retreat " + backupFile;
        Process process = Runtime.getRuntime().exec(javaCommand);
        int exitCode = process.waitFor();


//        server.shutdown();

    }
}
