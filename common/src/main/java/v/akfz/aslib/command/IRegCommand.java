package v.akfz.aslib.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public interface IRegCommand {
    void register(CommandDispatcher<CommandSourceStack> dispatcher);
}
