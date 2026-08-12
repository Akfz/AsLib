package v.akfz.aslib.render.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import v.akfz.aslib.render.camera.event.CameraEvent;
import v.akfz.aslib.render.camera.event.InfiniteCameraEvent;
import v.akfz.aslib.util.math.CompositePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraManager {
    public static CompositePair<Float> XRots = CompositePair.empty(Float.class);
    public static CompositePair<Float> YRots = CompositePair.empty(Float.class);
    public static CompositePair<Float> ZRots = CompositePair.empty(Float.class);

    public static CompositePair<Float> XPoses = CompositePair.empty(Float.class);
    public static CompositePair<Float> YPoses = CompositePair.empty(Float.class);
    public static CompositePair<Float> ZPoses = CompositePair.empty(Float.class);

    public static float rotSpeed = 15f;
    public static float posSpeed = 10f;

    private static final Map<String, CameraEvent> events = new HashMap<>();
    public static void addEvent(String id,CameraEvent event) {
        events.put(id,event);
    }
    public static void removeEvent(String id) {
        events.remove(id);
    }

    public static void update() {
        Minecraft client = Minecraft.getInstance();
        if (client.cameraEntity == null) {
            return;
        }

        events.values().forEach(CameraEvent::doPrevSet);
        ((CameraExtender) client.gameRenderer.getMainCamera()).addPos(new Vec3(XPoses.get(), YPoses.get(), ZPoses.get()));
        ((CameraExtender) client.gameRenderer.getMainCamera()).clearRots();
        ((CameraExtender) client.gameRenderer.getMainCamera()).addRot(new Vec3(XRots.get(), YRots.get(), ZRots.get()));
        List<String> toRemove = new ArrayList<>();
        events.forEach((id, camEvent) -> {
            camEvent.doAfterSet();

            if (!(camEvent instanceof InfiniteCameraEvent)) {
                toRemove.add(id);
            }
        });
        toRemove.forEach(events::remove);
    }
}
