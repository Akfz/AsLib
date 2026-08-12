package v.akfz.aslib.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import v.akfz.aslib.command.CommandHelper;
import v.akfz.aslib.command.IRegCommand;
import v.akfz.aslib.world.DimensionBuilder;
import v.akfz.aslib.world.DimensionHelper;
import v.akfz.aslib.world.preset.VanillaBiomes;
import v.akfz.aslib.world.preset.VanillaNoiseSettings;

public class DimensionCommand extends CommandHelper implements IRegCommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        var createVoidCmd = literal("create_void")
                .then(identifier("id")
                        .executes(ctx -> {
                            ResourceLocation id = getID(ctx, "id");

                            DimensionHelper.builder(id)
                                    .voidPreset()
                                    .register();

                            ctx.getSource().sendSuccess(() -> Component.literal("Created void dimension: " + id), true);
                            return 1;
                        }));

        var createFlatCmd = literal("create_flat")
                .then(identifier("id")
                        .executes(ctx -> {
                            ResourceLocation id = getID(ctx, "id");

                            DimensionHelper.builder(id)
                                    .generator(DimensionBuilder.GeneratorType.FLAT)
                                    .biome(VanillaBiomes.PLAINS)
                                    .enableStructures(false)
                                    .addLayer(new ResourceLocation("minecraft", "bedrock"), 1)
                                    .addLayer(new ResourceLocation("minecraft", "dirt"), 2)
                                    .addLayer(new ResourceLocation("minecraft", "grass_block"), 1)
                                    .register();

                            ctx.getSource().sendSuccess(() -> Component.literal("Created flat dimension: " + id), true);
                            return 1;
                        }));

        var createNoiseCmd = literal("create_noise")
                .then(identifier("id")
                        .executes(ctx -> {
                            ResourceLocation id = getID(ctx, "id");

                            DimensionHelper.builder(id)
                                    .generator(DimensionBuilder.GeneratorType.NOISE)
                                    .biome(VanillaBiomes.PLAINS)
                                    .noiseSettings(VanillaNoiseSettings.OVERWORLD)
                                    .enableStructures(true)
                                    .register();

                            ctx.getSource().sendSuccess(() -> Component.literal("Created noise dimension: " + id), true);
                            return 1;
                        }));

        var tpCmd = literal("tp")
                .then(identifier("id")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ResourceLocation id = getID(ctx, "id");

                            ServerLevel targetLevel = DimensionHelper.getLevel(player.level(), id);
                            if (targetLevel == null) {
                                ctx.getSource().sendFailure(Component.literal("Dimension is not loaded or does not exist: " + id));
                                return 0;
                            }

                            BlockPos spawn = targetLevel.getSharedSpawnPos();
                            double tpY = Math.max(spawn.getY(), -60.0);

                            DimensionHelper.teleport(player, targetLevel)
                                    .pos(spawn.getX() + 0.5, tpY, spawn.getZ() + 0.5)
                                    .resetFall(true)
                                    .playSound(SoundEvents.ENDERMAN_TELEPORT)
                                    .execute();

                            ctx.getSource().sendSuccess(() -> Component.literal("Teleported to spawn of dimension: " + id), true);
                            return 1;
                        })
                        .then(integer("x").then(integer("y").then(integer("z")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ResourceLocation id = getID(ctx, "id");
                                    int x = getInt(ctx, "x");
                                    int y = getInt(ctx, "y");
                                    int z = getInt(ctx, "z");

                                    ServerLevel targetLevel = DimensionHelper.getLevel(player.level(), id);
                                    if (targetLevel == null) {
                                        ctx.getSource().sendFailure(Component.literal("Dimension is not loaded: " + id));
                                        return 0;
                                    }

                                    DimensionHelper.teleport(player, targetLevel)
                                            .pos(x + 0.5, y, z + 0.5)
                                            .resetFall(true)
                                            .playSound(SoundEvents.ENDERMAN_TELEPORT)
                                            .execute();

                                    ctx.getSource().sendSuccess(() -> Component.literal("Teleported to " + id + " at coordinates " + x + ", " + y + ", " + z), true);
                                    return 1;
                                })))));

        var mainCmd = literal("aslib:dimension")
                .requires(hasLevel(2))
                .then(createVoidCmd)
                .then(createFlatCmd)
                .then(createNoiseCmd)
                .then(tpCmd);

        dispatcher.register(mainCmd);
    }
}