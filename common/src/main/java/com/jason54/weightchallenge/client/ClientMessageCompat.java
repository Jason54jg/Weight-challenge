package com.jason54.weightchallenge.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

/**
 * Compat 1.21.11 ({@code displayClientMessage}) / 26.x ({@code sendOverlayMessage}).
 */
public final class ClientMessageCompat {
    private static final Method ACTION_BAR;

    static {
        Method method = null;
        try {
            method = LocalPlayer.class.getMethod("sendOverlayMessage", Component.class);
        } catch (NoSuchMethodException ignored) {
            try {
                method = LocalPlayer.class.getMethod("displayClientMessage", Component.class, boolean.class);
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        ACTION_BAR = method;
    }

    private ClientMessageCompat() {}

    public static void showActionBar(LocalPlayer player, Component message) {
        try {
            if (ACTION_BAR.getParameterCount() == 1) {
                ACTION_BAR.invoke(player, message);
            } else {
                ACTION_BAR.invoke(player, message, true);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to show action bar", e);
        }
    }
}
