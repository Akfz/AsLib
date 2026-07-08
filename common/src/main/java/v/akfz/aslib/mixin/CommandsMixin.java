package v.akfz.aslib.mixin;

import com.mojang.brigadier.CommandDispatcher;
import v.akfz.aslib.command.CommandHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInitCommands(Commands.CommandSelection selection, net.minecraft.commands.CommandBuildContext context, CallbackInfo ci) {
        CommandHandler.setDispatcher(this.dispatcher);
    }
}