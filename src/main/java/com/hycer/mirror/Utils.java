package com.hycer.mirror;

import net.minecraft.text.Text;
import net.minecraft.world.World;

public class Utils {
    /**
     * 向全体玩家广播消息
     * @param world world对象
     * @param message 消息字符串
     */
    public static void broadcastToAllPlayers(World world, String message){
        if (world != null) {
            world.getPlayers().forEach(player -> player.sendMessage(Text.of(message)));
        }
    }
}
