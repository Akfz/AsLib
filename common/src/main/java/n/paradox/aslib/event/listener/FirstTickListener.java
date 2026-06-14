package n.paradox.aslib.event.listener;

import n.paradox.aslib.event.api.EventPriority;
import n.paradox.aslib.event.api.Listener;
import n.paradox.aslib.event.api.Subscribe;
import n.paradox.aslib.event.impl.client.ClientFirstTickEvent;
import n.paradox.aslib.event.impl.server.ServerFirstTickEvent;
import n.paradox.aslib.register.AsLibRegistries;

public final class FirstTickListener implements Listener {
    @Subscribe(priority = EventPriority.HIGHEST)
    public void onClientFirstTick(ClientFirstTickEvent event) {
        AsLibRegistries.Init();
    }
    @Subscribe(priority = EventPriority.HIGHEST)
    public void onServerFirstTick(ServerFirstTickEvent event) {
        AsLibRegistries.Init();
    }
}
