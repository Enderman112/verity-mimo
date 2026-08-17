package com.veritymimo.direct;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class MimoDirectNotifier {
    private MimoDirectNotifier() {
    }

    public static void notify(Player player, String message) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.m_5661_(Component.m_237113_(message).m_130940_(ChatFormatting.RED), false);
        player.m_5661_(Component.m_237113_(message).m_130940_(ChatFormatting.RED), true);
    }
}
