package n.paradox.aslib;

import n.paradox.aslib.event.api.EventBus;
import n.paradox.aslib.listener.FirstTickListener;
import n.paradox.aslib.register.AsLibRegistries;
import n.paradox.aslib.template.command.ReactionGameCommand;

public final class AsLib {
    public static final EventBus EVENT_BUS = new EventBus();
    public void init() {
        EVENT_BUS.register(new FirstTickListener());

        AsLibRegistries.getCommandRegistry().addCommand("aslib_testcom", new ReactionGameCommand());
    }
}