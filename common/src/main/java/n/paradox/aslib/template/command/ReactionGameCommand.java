package n.paradox.aslib.template.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import n.paradox.aslib.command.CommandHelper;
import n.paradox.aslib.command.IRegCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReactionGameCommand extends CommandHelper implements IRegCommand {
    private static final Map<UUID, Long> startTimes = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("aslib:testgame")
                        .executes(ctx -> {
                            startMiniGame(ctx);
                            return 1;
                        })
                        .then(literal("click")
                                .executes(this::handleScore))
        );
    }

    private void startMiniGame(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSystemMessage(Component.literal("§e[Игра] §fПриготовьтесь..."));

        int delay = 2 + (int) (Math.random() * 4);

        scheduler.schedule(() -> {
            try {
                ServerPlayer player = source.getPlayerOrException();
                UUID uuid = player.getUUID();
                startTimes.put(uuid, System.currentTimeMillis());

                MutableComponent clickMe = Component.literal("§6§l>>> ЖМИ СЮДА <<<")
                        .withStyle(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aslib:testgame click")
                        ));

                source.sendSystemMessage(clickMe);
            } catch (Exception e) {
                System.err.println("ASLib - ReactionGameCommand : Failed to get player for mini-game");
            }
        }, delay, TimeUnit.SECONDS);
    }

    private int handleScore(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        try {
            ServerPlayer player = source.getPlayerOrException();
            UUID uuid = player.getUUID();

            if (!startTimes.containsKey(uuid)) {
                source.sendSystemMessage(Component.literal("§cВы еще не начали игру!"));
                return 0;
            }

            long timeTaken = System.currentTimeMillis() - startTimes.get(uuid);
            startTimes.remove(uuid);

            source.sendSystemMessage(Component.literal("§aВаш результат: §f" + timeTaken + " мс")
                    .withStyle(ChatFormatting.BOLD));

        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("§cЭту команду может использовать только игрок!"));
            return 0;
        }

        return 1;
    }
}