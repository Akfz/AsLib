package v.akfz.aslib.initializer;

//Клиент или сервер
public class SideEnvironment {
    public enum Side {
        Client,
        Server
    }

    private static boolean isFreezed = false;
    private static Side currentSide = null;

    public static void setAndFreeze(Side side) {
        if (isFreezed) return;
        currentSide = side;
        isFreezed = true;
    }

    public static Side getCurrentSide() {
        return currentSide;
    }
}
