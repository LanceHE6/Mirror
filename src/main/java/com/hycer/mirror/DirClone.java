package com.hycer.mirror;

import net.minecraft.world.World;

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

    private final World world;
    DirClone(String sourcePath, String destinationPath, World world){
        this.world = world;
        this.sourceFolder = new File(sourcePath);
        this.destinationFolder = new File(destinationPath);

    }
    public void backup() throws IOException {
        totalNUm = 0;
        copied = 0;
        getTotalNum(sourceFolder);
        copyFolder(sourceFolder, destinationFolder);

    }

    /**
     * 获取总的文件数
     * @param sourceFolder 目标文件夹
     */
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

    /**
     * 复制文件夹
     * @param sourceFolder 源文件夹
     * @param destinationFolder 目的文件夹
     * @throws IOException IO异常
     */
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
                    Utils.broadcastToAllPlayers(world, "§aProgress:" + copied + " / " + totalNUm);
                }
            }
        }
    }

}


