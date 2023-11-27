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
        server.getPlayerManager().broadcast(Text.of(message), false);
    }
}
