package n.paradox.aslib.util;

import net.minecraft.client.Minecraft;

public final class GlobalUtils {
    public static boolean isClientSide() {
        try {
            return Minecraft.getInstance() != null;
        } catch (Exception e) { //в теории можно заменить на RuntimeException
            return false;
        }
    }
}
