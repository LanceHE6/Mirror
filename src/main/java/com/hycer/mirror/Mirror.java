package com.hycer.mirror;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
                                .then(CommandManager.literal("retreat").executes(context -> {
                                    // rollback 函数执行体
                                    context.getSource().sendMessage(Text.literal("调用 /mirror retreat" + context.getInput()));

                                    return 1;
                                }))
                )
        );

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
}
