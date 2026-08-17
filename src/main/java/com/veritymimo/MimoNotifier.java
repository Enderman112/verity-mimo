package com.veritymimo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class MimoNotifier {
    private MimoNotifier() {
    }

    public static void notify(Player player, String message) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.m_5661_(Component.m_237113_(message).m_130940_(ChatFormatting.RED), false);
        player.m_5661_(Component.m_237113_(message).m_130940_(ChatFormatting.RED), true);
    }
}
