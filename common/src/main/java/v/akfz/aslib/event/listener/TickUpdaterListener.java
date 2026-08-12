package v.akfz.aslib.event.listener;

import v.akfz.aslib.event.api.EventPriority;
import v.akfz.aslib.event.api.Listener;
import v.akfz.aslib.event.api.Subscribe;
import v.akfz.aslib.event.impl.TickUpdater;
import v.akfz.aslib.render.camera.CameraManager;

public class TickUpdaterListener implements Listener {
    @Subscribe(priority = EventPriority.HIGHEST)
    public void execute(TickUpdater event) {
        if (event.client) {
            CameraManager.update();
        }
    }
}
