package n.paradox.aslib.register;

import com.mojang.brigadier.CommandDispatcher;
import n.paradox.aslib.command.IRegCommand;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Регистрирует любую реализацию IRegCommand
// можно создать instance (для закрытой регистрации), но рекомендуется через AsLibRegistries, после register, добавить ничего нельзя, но можно взять instance(зарегистрированные)
public final class CommandRegistry implements IRegistry{
    private final Map<String, IRegCommand> registryMap = new HashMap<>(); //String - хелпер, он НЕ влияет на реггер, Блоки должны наследоваться RegisterObject
    private boolean isAllowToChange = true;

    public Map<String, IRegCommand> getRegistryMap() {
        return Collections.unmodifiableMap(this.registryMap);
    }

    @Override
    public boolean isAllowToChange() {
        return isAllowToChange;
    }

    @Override
    public void register() {
        if (!isAllowToChange) return;
        isAllowToChange = false;
    }

    public void processCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (IRegCommand command : registryMap.values()) {
            try {
                command.register(dispatcher);
            } catch (Exception e) {
                System.err.println("ASLib - CommandRegistry : Failed to register command: " + command.getClass().getName());
                e.printStackTrace();
            }
        }
    }

    public void addCommand(String helperID, IRegCommand command) {
        if (!isAllowToChange) return;
        if (command != null) {
            registryMap.put(helperID,command);
        } else {
            System.err.println("ASLib - CommandRegistry : Register " + helperID + " command is failed, command is null");
        }
    }

    public void removeCommand(String helperID) {
        if (!isAllowToChange) return;
        if (registryMap.remove(helperID) == null) {
            System.err.println("ASLib - CommandRegistry : Cant remove " + helperID + " (command), because its not exist");
        }
    }
}
