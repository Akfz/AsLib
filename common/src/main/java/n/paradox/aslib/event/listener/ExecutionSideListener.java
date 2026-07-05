package n.paradox.aslib.event.listener;

import n.paradox.aslib.event.api.Listener;
import n.paradox.aslib.event.api.Subscribe;
import n.paradox.aslib.event.impl.ExecutionSideEvent;
import n.paradox.aslib.util.GlobalUtils;

public final class ExecutionSideListener implements Listener {
    @Subscribe
    public void execute(ExecutionSideEvent event) {
    }
}
