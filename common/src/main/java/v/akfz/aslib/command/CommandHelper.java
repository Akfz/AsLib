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

/**
 * Helper for commands arguments
 */
public abstract class CommandHelper {

    /**
     * Creates a literal argument (subcommand name)
     */
    protected LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return Commands.literal(name);
    }

    /**
     * Creates a single-word string argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, String> string(String name) {
        return Commands.argument(name, StringArgumentType.string());
    }

    /**
     * Creates a greedy string argument (reads everything till the end)
     */
    protected RequiredArgumentBuilder<CommandSourceStack, String> greedyString(String name) {
        return Commands.argument(name, StringArgumentType.greedyString());
    }

    /**
     * Creates an integer argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, Integer> integer(String name) {
        return Commands.argument(name, IntegerArgumentType.integer());
    }

    /**
     * Creates a decimal (double) argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, Double> decimal(String name) {
        return Commands.argument(name, DoubleArgumentType.doubleArg());
    }

    /**
     * Creates a boolean argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, Boolean> bool(String name) {
        return Commands.argument(name, BoolArgumentType.bool());
    }

    /**
     * Creates a single player selector argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> player(String name) {
        return Commands.argument(name, EntityArgument.player());
    }

    /**
     * Creates a multiple players selector argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> players(String name) {
        return Commands.argument(name, EntityArgument.players());
    }

    /**
     * Creates a single entity selector argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entity(String name) {
        return Commands.argument(name, EntityArgument.entity());
    }

    /**
     * Creates a multiple entities selector argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entities(String name) {
        return Commands.argument(name, EntityArgument.entities());
    }

    /**
     * Creates a ResourceLocation (identifier) argument
     */
    protected RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> identifier(String name) {
        return Commands.argument(name, ResourceLocationArgument.id());
    }

    /**
     * Checks if the command source has specified permission level
     */
    protected Predicate<CommandSourceStack> hasLevel(int level) {
        return src -> src.hasPermission(level);
    }

    /**
     * Checks if the command source is a player
     */
    protected Predicate<CommandSourceStack> isPlayer() {
        return src -> src.getEntity() instanceof ServerPlayer;
    }

    /**
     * Checks if the command source is a player AND has permission level
     */
    protected Predicate<CommandSourceStack> hasLevelAndIsPlayer(int level) {
        return isPlayer().and(hasLevel(level));
    }

    /**
     * Gets string argument value from context
     */
    protected String getString(CommandContext<CommandSourceStack> ctx, String name) {
        return StringArgumentType.getString(ctx, name);
    }

    /**
     * Gets integer argument value from context
     */
    protected int getInt(CommandContext<CommandSourceStack> ctx, String name) {
        return IntegerArgumentType.getInteger(ctx, name);
    }

    /**
     * Gets double argument value from context
     */
    protected double getDouble(CommandContext<CommandSourceStack> ctx, String name) {
        return DoubleArgumentType.getDouble(ctx, name);
    }

    /**
     * Gets boolean argument value from context
     */
    protected boolean getBool(CommandContext<CommandSourceStack> ctx, String name) {
        return BoolArgumentType.getBool(ctx, name);
    }

    /**
     * Gets ResourceLocation argument value from context
     */
    protected ResourceLocation getID(CommandContext<CommandSourceStack> ctx, String name) {
        return ResourceLocationArgument.getId(ctx, name);
    }

    /**
     * Sets permission level requirement for literal builder
     */
    protected LiteralArgumentBuilder<CommandSourceStack> requires(LiteralArgumentBuilder<CommandSourceStack> builder, int level) {
        return builder.requires(src -> src.hasPermission(level));
    }

    /**
     * Sets action to execute for literal builder
     */
    protected LiteralArgumentBuilder<CommandSourceStack> executes(LiteralArgumentBuilder<CommandSourceStack> builder, Consumer<CommandContext<CommandSourceStack>> action) {
        return builder.executes(ctx -> {
            action.accept(ctx);
            return 1;
        });
    }

    /**
     * Sets action to execute for argument builder
     */
    protected <T> RequiredArgumentBuilder<CommandSourceStack, T> executes(RequiredArgumentBuilder<CommandSourceStack, T> builder, Consumer<CommandContext<CommandSourceStack>> action) {
        return builder.executes(ctx -> {
            action.accept(ctx);
            return 1;
        });
    }
}