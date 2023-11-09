package com.hycer.mirror;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
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

                                .then(literal("backup").then(argument("tag", greedyString())).executes(context -> {
                                    // backup 函数执行体
                                    System.out.println(1820303323);
//                                    String tag = getString(context, "tag");
//                                    System.out.println(tag);
                                    ServerCommandSource player =  context.getSource();
//                                    player.sendMessage(Text.of(tag));
                                    BackupManager backupManager = new BackupManager();
                                    try {
                                        backupManager.backup(player);
                                    } catch (IOException e) {
                                        player.sendMessage(Text.of(e.getMessage()));
                                        throw new RuntimeException(e);
                                    }
                                    return 1;
                                }))
                                .then(CommandManager.literal("retreat").executes(context -> {
                                    // rollback 函数执行体
                                    context.getSource().sendMessage(Text.literal("调用 /mirror retreat" + context.getInput()));

                                    return 1;
                                }))
                )
        );

    }
}
