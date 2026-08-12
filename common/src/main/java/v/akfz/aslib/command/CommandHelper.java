package v.akfz.aslib.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;
import java.util.function.Predicate;

//хелпер для создания команд
public abstract class CommandHelper {

    protected LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return Commands.literal(name);
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> string(String name) {
        return Commands.argument(name, StringArgumentType.string());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> greedyString(String name) {
        return Commands.argument(name, StringArgumentType.greedyString());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Integer> integer(String name) {
        return Commands.argument(name, IntegerArgumentType.integer());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Double> decimal(String name) {
        return Commands.argument(name, DoubleArgumentType.doubleArg());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Boolean> bool(String name) {
        return Commands.argument(name, BoolArgumentType.bool());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> player(String name) {
        return Commands.argument(name, EntityArgument.player());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> players(String name) {
        return Commands.argument(name, EntityArgument.players());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entity(String name) {
        return Commands.argument(name, EntityArgument.entity());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entities(String name) {
        return Commands.argument(name, EntityArgument.entities());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> identifier(String name) {
        return Commands.argument(name, ResourceLocationArgument.id());
    }

    protected Predicate<CommandSourceStack> hasLevel(int level) {
        return src -> src.hasPermission(level);
    }

    protected Predicate<CommandSourceStack> isPlayer() {
        return src -> src.getEntity() instanceof ServerPlayer;
    }

    protected Predicate<CommandSourceStack> hasLevelAndIsPlayer(int level) {
        return isPlayer().and(hasLevel(level));
    }

    protected String getString(CommandContext<CommandSourceStack> ctx, String name) {
        return StringArgumentType.getString(ctx, name);
    }

    protected int getInt(CommandContext<CommandSourceStack> ctx, String name) {
        return IntegerArgumentType.getInteger(ctx, name);
    }

    protected double getDouble(CommandContext<CommandSourceStack> ctx, String name) {
        return DoubleArgumentType.getDouble(ctx, name);
    }

    protected boolean getBool(CommandContext<CommandSourceStack> ctx, String name) {
        return BoolArgumentType.getBool(ctx, name);
    }

    protected ResourceLocation getID(CommandContext<CommandSourceStack> ctx, String name) {
        return ResourceLocationArgument.getId(ctx, name);
    }

    protected LiteralArgumentBuilder<CommandSourceStack> requires(LiteralArgumentBuilder<CommandSourceStack> builder, int level) {
        return builder.requires(src -> src.hasPermission(level));
    }

    protected LiteralArgumentBuilder<CommandSourceStack> executes(LiteralArgumentBuilder<CommandSourceStack> builder, Consumer<CommandContext<CommandSourceStack>> action) {
        return builder.executes(ctx -> {
            action.accept(ctx);
            return 1;
        });
    }

    protected <T> RequiredArgumentBuilder<CommandSourceStack, T> executes(RequiredArgumentBuilder<CommandSourceStack, T> builder, Consumer<CommandContext<CommandSourceStack>> action) {
        return builder.executes(ctx -> {
            action.accept(ctx);
            return 1;
        });
    }
}