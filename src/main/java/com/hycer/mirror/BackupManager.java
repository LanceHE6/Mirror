package com.hycer.mirror;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class BackupManager {
    private String backupPath = Constants.BACKUP_PATH;
    private final String worldPath = Constants.WORLD_PATH;

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

    /**
     * 备份操作
     * @param context 命令源上下文
     * @param haveTag 是否有备份tag
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    public void backup(CommandContext<ServerCommandSource> context, boolean haveTag) throws IOException, InterruptedException {
        ServerCommandSource source =  context.getSource();
        MinecraftServer server = source.getServer();
//        ServerWorld world = server.getWorld(source.getWorld().getRegistryKey());
        String playerName = source.getName();
        System.out.println(System.getProperty("os.name"));
        Utils.broadcastToAllPlayers(server, "§b[Mirror]§c %s §6发起备份请求 服务器将在 §c5s §6后进行地图备份！".formatted(playerName));
        Runnable task = () -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            double startTime =  System.currentTimeMillis();
            checkBackupCount(server); // 检查备份数量
            source.getServer().saveAll(false, false, false);
            Utils.broadcastToAllPlayers(server, "§b[Mirror]§6游戏数据已保存，开始地图备份");
//            System.out.println("进行服务器备份");
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
            DirClone dirClone = new DirClone(worldPath, backupPath, server);
            try {
                dirClone.backup();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            double duration = (System.currentTimeMillis() - startTime) / 1000;
            Utils.broadcastToAllPlayers(server, "§b[Mirror]§6备份完成：" + backupPath + "  耗时 " + String.format("%.2f", duration) + " s");
            Mirror.setBackupExecuting(false);
//            System.out.println("备份完成");
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);

        // 关闭线程池
        executor.shutdown();
    }
    public void autoBackup(MinecraftServer server) {
        Utils.broadcastToAllPlayers(server, "§b[Mirror]§6开始执行§c 自动备份 §6 服务器将在 §c5s §6后进行地图备份！");
        Runnable task = () -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            double startTime = System.currentTimeMillis();
            checkBackupCount(server); // 检查备份数量
            server.saveAll(false, false, false);
            Utils.broadcastToAllPlayers(server, "§b[Mirror]§6游戏数据已保存，开始地图备份");

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 获取当前日期时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            LocalDateTime localDateTime = LocalDateTime.now();
            String nowTime = localDateTime.format(formatter);

            backupPath += nowTime;

            DirClone dirClone = new DirClone(worldPath, backupPath, server);
            try {
                dirClone.backup();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            double duration = (System.currentTimeMillis() - startTime) / 1000;
            Utils.broadcastToAllPlayers(server, "§b[Mirror]§6备份完成：" + backupPath +  "  耗时 " + String.format("%.2f", duration) + " s");
            Mirror.setBackupExecuting(false);
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);

        // 关闭线程池
        executor.shutdown();
    }

    /**
     * 检查备份数量上限
     * @param server MinecraftServer对象用于发送通知
     */
    private void checkBackupCount(MinecraftServer server) {
        File backupDir = new File(backupPath);
        File[] backupFiles = backupDir.listFiles(File::isDirectory);
        int maxBackups = new ModConfiguration().getMaxBackupFiles();
        if (backupFiles != null && backupFiles.length >= maxBackups) {
            // 根据文件的最后修改时间进行排序
            Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

            // 删除最早的备份
            int numToDelete = backupFiles.length - maxBackups + 1;
            for (int i = 0; i < numToDelete; i++) {
                File fileToDelete = backupFiles[i];
                deleteBackup(fileToDelete);
                Utils.broadcastToAllPlayers(server, "§b[Mirror]§6已达到最大备份数量: %d §4删除溢出备份: §6".formatted(maxBackups) + fileToDelete.getName());
//                System.out.println("已删除溢出备份" + fileToDelete.getName());
            }
        }
    }

    /**
     * 删除指定备份
     * @param backupFile 目标备份
     */
    public void deleteBackup(File backupFile) {
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
            System.out.println("删除文件夹失败");
        }
    }

    /**
     * 获取所有备份
     * @return 备份数组
     */
    public Map<String, String> getBackupList() {
        File backupDir = new File(backupPath);
        File[] backupFiles = backupDir.listFiles(File::isDirectory);
        // LinkedHashMap会保留数据的插入顺序
        Map<String, String> backupMap = new LinkedHashMap<>();
        // 根据备份时间排序
        if (backupFiles != null) {
            // 使用自定义的比较器进行排序
            Arrays.sort(backupFiles, (file1, file2) -> {
                try {
                    BasicFileAttributes fileAttributes1 = Files.readAttributes(file1.toPath(), BasicFileAttributes.class);
                    BasicFileAttributes fileAttributes2 = Files.readAttributes(file2.toPath(), BasicFileAttributes.class);
                    FileTime creationTime1 = fileAttributes1.creationTime();
                    FileTime creationTime2 = fileAttributes2.creationTime();
                    return creationTime2.compareTo(creationTime1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            });
            for (File file : backupFiles) {
                String backupName = file.getName();
                try {
                    BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    FileTime creationTime = fileAttributes.creationTime();
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedCreationTime = formatter.format(localDateTime);
                    backupMap.put(backupName, formattedCreationTime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }

            return backupMap;
        }
        return backupMap;
    }

    /**
     * 回档操作
     * @param context 命令源上下文
     * @param backupFile 目标备份
     */
    public void rollback(CommandContext<ServerCommandSource> context, String backupFile) throws IOException {
        File sourceBackup = new File(backupPath + backupFile);
        if (!sourceBackup.exists() || !sourceBackup.isDirectory()) {
            Utils.broadcastToAllPlayers(context.getSource().getServer(), "§b[Mirror]§4指定的备份文件不存在或不是文件夹");
            System.out.println("指定的备份文件不存在");
            return;
        }
        Utils.broadcastToAllPlayers(context.getSource().getServer(), "§b[Mirror]§6服务端将在§4 10s §6后关闭并回档，请等待重启");

        MinecraftServer server = context.getSource().getServer();

        String rollbackPath = System.getProperty("user.dir") + "/" + backupPath + Constants.ROLLBACK_SCRIPT_FILE;
        System.out.println(System.getProperty("user.dir"));

        ProcessBuilder processBuilder = null;

        if(Constants.SYSTEM_TYPE == 1){
            processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", rollbackPath + " " + backupFile);
        } else {

            System.out.println("执行回档脚本");
            Runtime.getRuntime().exec("chmod +x " + rollbackPath);
            processBuilder = new ProcessBuilder("/bin/sh", "-c", rollbackPath + " " + backupFile);
        }
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("shutdown")){
                    server.stop(false);
                    break;
                }
                System.out.println(line); // 打印新进程的输出信息
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.stop(false);


    }
}
