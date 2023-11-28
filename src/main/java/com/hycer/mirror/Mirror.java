package com.hycer.mirror;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;


public class Mirror implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        initialize();
        registerCommands();
    }

    public void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("mirror")
                                .requires(sources -> sources.hasPermissionLevel(2))
                                .then(literal("backup")
                                        .then(argument("tag", StringArgumentType.string())
                                                .executes(context -> executeBackup(context, true)))
                                        .executes(context -> executeBackup(context, false))
                                )
                                .then(literal("backup-list").executes(this::executeBackList))
                                .then(literal("retreat")
                                        .then(argument("backup", StringArgumentType.string())
                                                .executes(context -> {
                                                    try {
                                                        executeRetreat(context);
                                                        return 1;
                                                    } catch (IOException | InterruptedException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                })))
                                .then(literal("auto-backup")
                                        .then(literal("true")
                                                .executes(context -> {
                                                    executeSetAutoBackup(context, true);
                                                    return 1;
                                                }))
                                        .then(literal("false")
                                                .executes(context -> {
                                                    executeSetAutoBackup(context, false);
                                                    return 1;
                                                }))
                                        .executes(context -> {
                                    executeAutoBackup(context);
                                    return 1;
                                })
        )));

    }
    private void initialize(){
        // 备份文件夹检验
        File backupDir = new File(Constants.BACKUP_PATH);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdir();
            if (created){
                System.out.println("backup directory created successfully");
            } else {
                System.out.println("backup directory creation failed");
            }
        }
        // 配置文件夹校验
        File configDir = new File(Constants.CONFIG_PATH);
        if (!configDir.exists()) {
            boolean created = configDir.mkdir();
            if (created){
                System.out.println("config directory created successfully");
            } else {
                System.out.println("config directory creation failed");
            }
        }
        // 判断是否存在 retreat.bat 文件，若不存在则创建
        File retreatBatFile = new File(Constants.BACKUP_PATH + Constants.RETREAT_BAT_FILE);
        if (!retreatBatFile.exists()) {
            try {
                boolean created = retreatBatFile.createNewFile();
                if (created) {
                    System.out.println("retreat.bat 回档脚本创建成功");
                } else {
                    System.out.println("retreat.bat 回档脚本创建失败");
                }
                Script.writeRetreatBatFile(retreatBatFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 注册时间监听器用于自动备份
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            LocalTime currentTime = LocalTime.now();
            ModConfiguration modConfig = new ModConfiguration();

            if (modConfig.isAutoBackup() && currentTime.getHour() == modConfig.getAutoBackupTime() && currentTime.getMinute() == 0 && currentTime.getSecond() == 0) {
                BackupManager backupManager = new BackupManager();
                backupManager.autoBackup(server);
            }
        });
    }
    public  int executeBackup(CommandContext<ServerCommandSource> context, boolean haveTag){
        ServerCommandSource player =  context.getSource();
        BackupManager backupManager = new BackupManager();
        try {
            backupManager.backup(context, haveTag);
        } catch (IOException e) {
            player.sendMessage(Text.of(e.getMessage()));
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    public int executeBackList(CommandContext<ServerCommandSource> context){
        BackupManager backupManager = new BackupManager();
        ServerCommandSource player = context.getSource();
        Map<String, String> backupList = backupManager.getBackupList();
        player.sendMessage(Text.of("§b[Mirror]§6地图备份"));
        for (Map.Entry<String, String> entry : backupList.entrySet()) {
            String backupName = entry.getKey();
            String creationTime = entry.getValue();
            player.sendMessage(Text.of("§e备份名: §a" + backupName + "  §e备份时间: §a" + creationTime));
        }
        return 1;
    }

    public void executeRetreat(CommandContext<ServerCommandSource> context) throws IOException, InterruptedException {
        BackupManager backupManager = new BackupManager();
        String backupFile = StringArgumentType.getString(context, "backup");
        backupManager.retreat(context, backupFile);
    }

    public void executeAutoBackup(CommandContext<ServerCommandSource> context){
        ModConfiguration modConfig = new ModConfiguration();
        context.getSource().sendMessage(Text.of("§b[Mirror]§6当前自动备份状态为:" +  (modConfig.isAutoBackup() ? "§a": "§c") + (modConfig.isAutoBackup() ? " true": " false")));
    }

    public void executeSetAutoBackup(CommandContext<ServerCommandSource> context, boolean status){
        ModConfiguration modConfig = new ModConfiguration();
        if (status == modConfig.isAutoBackup()){
            context.getSource().sendMessage(Text.of("§b[Mirror]§6当前自动备份状态已经为:" +  (modConfig.isAutoBackup() ? "§a": "§c") + (modConfig.isAutoBackup() ? " true": " false")));
        } else {
            modConfig.setAutoBackup(status);
            context.getSource().sendMessage(Text.of("§b[Mirror]§6当前自动备份状态已设置为:" +  (modConfig.isAutoBackup() ? "§a": "§c") + (modConfig.isAutoBackup() ? " true": " false")));
        }
    }
}
