package com.hycer.mirror;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.io.IOException;

import static net.minecraft.server.command.CommandManager.*;


public class Mirror implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        registerCommands();
    }

    public void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("mirror")
                                .requires(sources -> sources.hasPermissionLevel(4))
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
                                                }))
                )
        ));

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
        World world = context.getSource().getWorld();
        Object[] backupList = backupManager.getBackupList();
        Utils.broadcastToAllPlayers(world,  "§b[Mirror]§6地图备份:" );
        for (Object backup: backupList){
            Utils.broadcastToAllPlayers(world,  "§a" + backup.toString().replace(backupManager.getBackupDir(), ""));
        }
        return 1;
    }

    public void executeRetreat(CommandContext<ServerCommandSource> context) throws IOException, InterruptedException {
        BackupManager backupManager = new BackupManager();
        String backupFile = StringArgumentType.getString(context, "backup");
        backupManager.retreat(context, backupFile);
    }
}
