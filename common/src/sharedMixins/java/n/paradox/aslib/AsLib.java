package n.paradox.aslib;

import n.paradox.aslib.event.api.EventBus;
import n.paradox.aslib.event.impl.FirstTickEvent;
import n.paradox.aslib.initializer.generator.GenerateInitializer;
import n.paradox.aslib.initializer.generator.InitializerClass;
import n.paradox.aslib.initializer.generator.LoaderType;
import n.paradox.aslib.event.listener.ExecutionSideListener;
import n.paradox.aslib.event.listener.FirstTickListener;
import n.paradox.aslib.register.AsLibRegistries;
import n.paradox.aslib.template.command.ReactionGameCommand;

//вообще этого кода тут быть не должно, но src/main добавляется только в конце компиляции из-за чего GenerateInitializer не вызывается, а src/generated изначально только для resources,
//там классов быть не должно, но в теории можно и туда
@GenerateInitializer(loader = LoaderType.Both, modId = "aslib")
public final class AsLib implements InitializerClass {
    public static final EventBus EVENT_BUS = new EventBus();
    @Override
    public void init() {
        EVENT_BUS.register(new FirstTickListener());
        EVENT_BUS.register(new ExecutionSideListener());

        FirstTickEvent.registerStartTickClient(AsLibRegistries::Init);

        AsLibRegistries.getCommandRegistry().addCommand("aslib_testcom", new ReactionGameCommand());
    }
}