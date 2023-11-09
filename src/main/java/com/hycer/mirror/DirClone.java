package com.hycer.mirror;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 文件复制
 */
public class DirClone {
    private final File sourceFolder;
    private final File destinationFolder;
    private static long totalNUm = 0;
    private static long copied = 0;

    private final ServerCommandSource player;
    DirClone(String sourcePath, String destinationPath, ServerCommandSource player){
        this.player = player;
        this.sourceFolder = new File(sourcePath);
        this.destinationFolder = new File(destinationPath);

    }
    public void backup() throws IOException {
        getTotalNum(sourceFolder);
        System.out.println("cloning...");
        player.sendMessage(Text.of("cloning..."));
        copyFolder(sourceFolder, destinationFolder);
        System.out.println("clone successfully");
        player.sendMessage(Text.of("clone successfully"));

    }

    public static void getTotalNum(File sourceFolder){
        File[] files = sourceFolder.listFiles();
        if(files != null){
            for (File file : files){
                if (file.isDirectory()){
                    getTotalNum(file);
                } else {
                    // 跳过文件锁
                    if (file.getName().equals("session.lock")){
                        continue;
                    }
                    totalNUm ++;
                }
            }
        }
    }

    public void copyFolder(File sourceFolder, File destinationFolder) throws IOException{
        if (!destinationFolder.exists()){
            destinationFolder.mkdir();
        }
        File[] files = sourceFolder.listFiles();
        if(files != null){
            for (File file : files){
                if (file.isDirectory()){
                    copyFolder(file, new File(destinationFolder, file.getName()));
                } else {
                    // 跳过文件锁
                    if (file.getName().equals("session.lock")){
                        continue;
                    }
                    File destinationFile = new File(destinationFolder, file.getName());
                    Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copied ++;
                    System.out.println("progress:" + copied + " / " + totalNUm);
                    player.sendMessage(Text.of("progress:" + copied + " / " + totalNUm));
                }
            }
        }
    }

}


