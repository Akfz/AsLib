package v.akfz.aslib.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Fluent builder for executing entity teleportation across positions and dimensions.
 */
public class TeleportBuilder {
    private final Entity entity;
    private final ServerLevel targetLevel;
    private Vec3 pos = Vec3.ZERO;
    private float yaw = 0.0f;
    private float pitch = 0.0f;

    private boolean resetFallDistance = true;
    private boolean keepVelocity = false;
    private SoundEvent sound = null;

    public TeleportBuilder(Entity entity, ServerLevel targetLevel) {
        this.entity = entity;
        this.targetLevel = targetLevel;
        if (entity != null) {
            this.pos = entity.position();
            this.yaw = entity.getYRot();
            this.pitch = entity.getXRot();
        }
    }

    /**
     * Sets target coordinates.
     */
    public TeleportBuilder pos(double x, double y, double z) {
        this.pos = new Vec3(x, y, z);
        return this;
    }

    /**
     * Sets target position vector.
     */
    public TeleportBuilder pos(Vec3 pos) {
        if (pos != null) this.pos = pos;
        return this;
    }

    /**
     * Sets target yaw and pitch rotation.
     */
    public TeleportBuilder rot(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return this;
    }

    /**
     * Sets whether fall distance should be reset to zero upon teleportation.
     */
    public TeleportBuilder resetFall(boolean reset) {
        this.resetFallDistance = reset;
        return this;
    }

    /**
     * Sets whether entity velocity should be preserved after teleportation.
     */
    public TeleportBuilder keepVelocity(boolean keep) {
        this.keepVelocity = keep;
        return this;
    }

    /**
     * Sets a sound event to play at destination.
     */
    public TeleportBuilder playSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Executes teleportation.
     *
     * @return Teleported entity instance (may differ if dimension change recreates entity).
     */
    public Entity execute() {
        if (entity == null || targetLevel == null) return null;

        Vec3 velocity = entity.getDeltaMovement();

        if (resetFallDistance) {
            entity.resetFallDistance();
        }

        Entity resultEntity;

        if (entity instanceof ServerPlayer player) {
            player.teleportTo(targetLevel, pos.x, pos.y, pos.z, yaw, pitch);
            resultEntity = player;
        } else {
            if (entity.level() != targetLevel) {
                entity.teleportTo(pos.x, pos.y, pos.z);
                entity.setYRot(yaw);
                entity.setXRot(pitch);
                resultEntity = entity.changeDimension(targetLevel);
            } else {
                entity.teleportTo(pos.x, pos.y, pos.z);
                entity.setYRot(yaw);
                entity.setXRot(pitch);
                resultEntity = entity;
            }
        }

        if (resultEntity != null) {
            if (keepVelocity) {
                resultEntity.setDeltaMovement(velocity);
            } else {
                resultEntity.setDeltaMovement(Vec3.ZERO);
            }

            if (sound != null) {
                targetLevel.playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        sound,
                        SoundSource.PLAYERS,
                        1.0f, 1.0f
                );
            }
        }

        return resultEntity;
    }
}