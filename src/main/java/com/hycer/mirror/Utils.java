package com.hycer.mirror;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class Utils {
    /**
     * 向全体玩家广播消息
     * @param server MinecraftServer对象
     * @param message 消息字符串
     */
    public static void broadcastToAllPlayers(MinecraftServer server, String message){
        broadcastToAllPlayers(server, message, false);
    }
    /**
     * 向全体玩家广播消息
     * @param server MinecraftServer对象
     * @param message 消息字符串
     * @param overlay 是否覆盖显示
     */
    public static void broadcastToAllPlayers(MinecraftServer server, String message, boolean overlay){
        server.getPlayerManager().broadcast(Text.of(message), overlay);
    }
}
