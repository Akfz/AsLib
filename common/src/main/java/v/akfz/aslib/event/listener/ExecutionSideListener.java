package v.akfz.aslib.event.listener;

import v.akfz.aslib.event.api.Listener;
import v.akfz.aslib.event.api.Subscribe;
import v.akfz.aslib.event.impl.ExecutionSideEvent;

public final class ExecutionSideListener implements Listener {
    @Subscribe
    public void execute(ExecutionSideEvent event) {
    }
}
