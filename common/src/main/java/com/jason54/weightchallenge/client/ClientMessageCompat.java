package com.jason54.weightchallenge.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Shows action-bar / overlay text across mappings.
 * <p>
 * Fabric ships intermediary names at runtime, so Mojang method-name reflection fails.
 * Resolve by signature instead: {@code (Component, boolean)} on 1.21.x,
 * or a one-arg {@code Component} method whose name contains {@code overlay} on 26.x.
 */
public final class ClientMessageCompat {
    private static final Method ACTION_BAR;
    private static final boolean OVERLAY_FLAG;

    static {
        Method method = null;
        boolean overlayFlag = false;

        for (Method candidate : LocalPlayer.class.getMethods()) {
            if (candidate.getReturnType() != void.class) {
                continue;
            }
            Class<?>[] params = candidate.getParameterTypes();
            if (params.length == 2
                    && params[0] == Component.class
                    && params[1] == boolean.class) {
                method = candidate;
                overlayFlag = true;
                break;
            }
        }

        if (method == null) {
            for (Method candidate : LocalPlayer.class.getMethods()) {
                if (candidate.getReturnType() != void.class) {
                    continue;
                }
                Class<?>[] params = candidate.getParameterTypes();
                if (params.length == 1 && params[0] == Component.class) {
                    String name = candidate.getName().toLowerCase(Locale.ROOT);
                    if (name.contains("overlay")) {
                        method = candidate;
                        break;
                    }
                }
            }
        }

        ACTION_BAR = method;
        OVERLAY_FLAG = overlayFlag;
    }

    private ClientMessageCompat() {}

    public static void showActionBar(LocalPlayer player, Component message) {
        if (ACTION_BAR == null) {
            return;
        }
        try {
            if (OVERLAY_FLAG) {
                ACTION_BAR.invoke(player, message, true);
            } else {
                ACTION_BAR.invoke(player, message);
            }
        } catch (ReflectiveOperationException ignored) {
            // HUD is best-effort; never crash the client over it.
        }
    }
}
