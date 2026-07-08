package v.akfz.aslib.event.listener;

import v.akfz.aslib.event.api.EventPriority;
import v.akfz.aslib.event.api.Listener;
import v.akfz.aslib.event.api.Subscribe;
import v.akfz.aslib.event.impl.FirstTickEvent;

public final class FirstTickListener implements Listener {
    @Subscribe(priority = EventPriority.HIGHEST)
    public void execute(FirstTickEvent event) {
    }
}
