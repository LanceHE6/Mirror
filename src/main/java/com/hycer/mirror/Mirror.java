package com.hycer.mirror;

import com.mojang.brigadier.arguments.IntegerArgumentType;
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
    private static boolean backupExecuting = false;

    @Override
    public void onInitialize() {
        initialize();
        registerCommands();
    }
    public static void setBackupExecuting(boolean flag){
        backupExecuting = flag;
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
                        .then(literal("backupList").executes(this::executeBackList))
                        .then(literal("retreat")
                                .then(argument("backupName", StringArgumentType.string())
                                        .executes(context -> {
                                            try {
                                                executeRetreat(context);
                                                return 1;
                                            } catch (IOException | InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })))
                        .then(literal("autoBackup")
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
                    )
                        .then(literal("maxBackupFiles")
                                .executes(context -> {
                                    executeMaxBackupFiles(context);
                                    return 1;
                                })
                                .then(argument("value", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            executeSetMaxBackupFiles(context);
                                            return 1;
                                        })))
                        .then(literal("autoBackupTime")
                                .executes(context -> {
                                    executeAutoBackupTime(context);
                                    return 1;
                                })
                                .then(argument("time", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            executeSetAutoBackupTime(context);
                                            return 1;
                                        })))
                        .then(literal("deleteBackup")
                                .then(argument("backupName", StringArgumentType.string())
                                        .executes(context -> {
                                            executeDeleteBackup(context);
                                            return 1;
                                        })))
                ));
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
        final boolean[] executed = {false};
        // 注册时间监听器用于自动备份
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            LocalTime currentTime = LocalTime.now();
            ModConfiguration modConfig = new ModConfiguration();

            if (modConfig.isAutoBackup() && currentTime.getHour() == modConfig.getAutoBackupTime() && currentTime.getMinute() == 0 && currentTime.getSecond() == 0) {
                if (!executed[0]) {
                    if (backupExecuting){
                        Utils.broadcastToAllPlayers(server, "§b[Mirror]§4当前正在进行地图备份，自动备份已取消");
                    } else {
                        backupExecuting = true;
                        BackupManager backupManager = new BackupManager();
                        backupManager.autoBackup(server);
                    }
                    executed[0] = true;
                }
            } else {
                executed[0] = false;
            }
        });
    }
    public  int executeBackup(CommandContext<ServerCommandSource> context, boolean haveTag){
        ServerCommandSource player =  context.getSource();
        if (backupExecuting){
            player.sendMessage(Text.of("§b[Mirror]§4当前正在进行地图备份，请勿频繁操作！"));
            return 1;
        } else {
            backupExecuting = true;
        }
        BackupManager backupManager = new BackupManager();
        try {
            backupManager.backup(context, haveTag);
        } catch (IOException e) {
            player.sendMessage(Text.of(e.getMessage()));
            backupExecuting = false;
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            backupExecuting = false;
            throw new RuntimeException(e);
        }
        return 1;
    }

    public int executeBackList(CommandContext<ServerCommandSource> context){
        BackupManager backupManager = new BackupManager();
        ServerCommandSource player = context.getSource();
        Map<String, String> backupList = backupManager.getBackupList();
        if (backupList.isEmpty()){
            player.sendMessage(Text.of("§b[Mirror]§6当前还没有地图备份哦！"));
            return 1;
        }
        player.sendMessage(Text.of("§b[Mirror]§6地图备份"));
        int count = 1;
        for (Map.Entry<String, String> entry : backupList.entrySet()) {
            String backupName = entry.getKey();
            String creationTime = entry.getValue();
            player.sendMessage(Text.of("§e%d.备份名: §a".formatted(count) + backupName + "  §e备份时间: §a" + creationTime));
            count ++;
        }
        return 1;
    }

    public void executeRetreat(CommandContext<ServerCommandSource> context) throws IOException, InterruptedException {
        File startBatFile = new File(Constants.START_BAT_FILE);
        if (!startBatFile.exists()){
            context.getSource().sendMessage(Text.of("§b[Mirror]§4未检测到服务端启动脚本§4§o start.bat §4无法使用回档功能！"));
            return;
        }
        BackupManager backupManager = new BackupManager();
        String backupFile = StringArgumentType.getString(context, "backupName");
        backupManager.retreat(context, backupFile);
    }

    public void executeAutoBackup(CommandContext<ServerCommandSource> context){
        ModConfiguration modConfig = new ModConfiguration();
        context.getSource().sendMessage(Text.of("§b[Mirror]§6当前自动备份状态为:" +  (modConfig.isAutoBackup() ? "§a": "§c") + (modConfig.isAutoBackup() ? " true": " false")));
    }
    public void executeAutoBackupTime(CommandContext<ServerCommandSource> context){
        ModConfiguration modConfig = new ModConfiguration();
        context.getSource().sendMessage(Text.of("§b[Mirror]§6当前自动备份时间为:每天 §a%d §6点".formatted(modConfig.getAutoBackupTime())));
    }
    public void executeMaxBackupFiles(CommandContext<ServerCommandSource> context){
        ModConfiguration modConfig = new ModConfiguration();
        context.getSource().sendMessage(Text.of("§b[Mirror]§6当前最大保留备份数为: §a" + modConfig.getMaxBackupFiles()));
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
    public void executeSetMaxBackupFiles(CommandContext<ServerCommandSource> context){
        int setNum = IntegerArgumentType.getInteger(context, "value");
        ServerCommandSource player = context.getSource();
        if (setNum < 1){
            player.sendMessage(Text.of("§b[Mirror]§4非法数值！"));
            return;
        }
        if (setNum > 20){
            player.sendMessage(Text.of("§b[Mirror]§4超过最大保留备份数量20限制！"));
            return;
        }
        ModConfiguration modConfig = new ModConfiguration();
        modConfig.setMaxBackupFiles(setNum);
        player.sendMessage(Text.of("§b[Mirror]§6最大保留备份数已设为：§a" + setNum));
    }
    public void executeSetAutoBackupTime(CommandContext<ServerCommandSource> context){
        int setTime = IntegerArgumentType.getInteger(context, "time");
        ServerCommandSource player = context.getSource();
        if (setTime < 0 || setTime > 23){
            player.sendMessage(Text.of("§b[Mirror]§4你只能输入 §60~23 §4的整数！"));
            return;
        }
        ModConfiguration modConfig = new ModConfiguration();
        modConfig.setAutoBackupTime(setTime);
        player.sendMessage(Text.of("§b[Mirror]§6自动备份时间已设为:每天 §a%d §6点".formatted(setTime)));
    }
    public void executeDeleteBackup(CommandContext<ServerCommandSource> context){
        String backupName = StringArgumentType.getString(context, "backupName");
        ServerCommandSource player = context.getSource();
        File targetBackup = new File(Constants.BACKUP_PATH + backupName);
        if (!targetBackup.exists()){
            player.sendMessage(Text.of("§b[Mirror]§4指定的备份文件不存在或不是文件夹"));
            return;
        }
        BackupManager backupManager = new BackupManager();
        backupManager.deleteBackup(targetBackup);
        player.sendMessage(Text.of("§b[Mirror]§6备份: §6 " + backupName + "§4已删除！"));
    }
}

