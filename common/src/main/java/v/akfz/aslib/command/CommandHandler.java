package v.akfz.aslib.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;

public final class CommandHandler {
    private CommandHandler() {}

    private static final List<IRegCommand> commands = new ArrayList<>();
    private static CommandDispatcher<CommandSourceStack> activeDispatcher;

    public static synchronized void setDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        activeDispatcher = dispatcher;

        for (IRegCommand command : commands) {
            try {
                command.register(dispatcher);
            } catch (Exception e) {
                System.err.println("[AsLib] Failed to register pending command: " + command.getClass().getName());
                e.printStackTrace();
            }
        }
    }

    public static synchronized void addCommand(IRegCommand command) {
        if (command == null) return;

        if (!commands.contains(command)) {
            commands.add(command);
        }

        if (activeDispatcher != null) {
            try {
                command.register(activeDispatcher);
            } catch (Exception e) {
                System.err.println("[AsLib] Failed to register late command: " + command.getClass().getName());
                e.printStackTrace();
            }
        }
    }

    public static List<IRegCommand> getCommands() {
        return new ArrayList<>(commands);
    }
}