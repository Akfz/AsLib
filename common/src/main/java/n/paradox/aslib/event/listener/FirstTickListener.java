package n.paradox.aslib.event.listener;

import n.paradox.aslib.event.api.EventPriority;
import n.paradox.aslib.event.api.Listener;
import n.paradox.aslib.event.api.Subscribe;
import n.paradox.aslib.event.impl.FirstTickEvent;
import n.paradox.aslib.util.GlobalUtils;

public final class FirstTickListener implements Listener {
    @Subscribe(priority = EventPriority.HIGHEST)
    public void execute(FirstTickEvent event) {
        if (GlobalUtils.isClientSide()) {
            FirstTickEvent.getOnStartTickClient().forEach(Runnable::run);
        } else {
            FirstTickEvent.getOnStartTickServer().forEach(Runnable::run);
        }
    }
}
