package v.akfz.aslib.datagen.damagetype;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import v.akfz.aslib.datagen.api.DataSerializable;

import java.nio.file.Path;

public class DamageTypeData extends DataSerializable {
    private String messageId;
    private DamageScaling scaling = DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
    private float exhaustion = 0.1f;
    private DamageEffects effects = DamageEffects.HURT;
    private DeathMessageType deathMessageType = DeathMessageType.DEFAULT;

    public DamageTypeData(ResourceLocation damageTypeId) {
        super(new ResourceLocation(damageTypeId.getNamespace(), "damage_type/" + damageTypeId.getPath()));
        this.messageId = damageTypeId.getNamespace() + "." + damageTypeId.getPath();
    }

    public DamageTypeData messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public DamageTypeData scaling(DamageScaling scaling) {
        this.scaling = scaling;
        return this;
    }

    public DamageTypeData exhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
        return this;
    }

    public DamageTypeData effects(DamageEffects effects) {
        this.effects = effects;
        return this;
    }

    public DamageTypeData deathMessageType(DeathMessageType deathMessageType) {
        this.deathMessageType = deathMessageType;
        return this;
    }

    @Override
    public boolean isAsset() {
        return false;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("message_id", messageId);
        json.addProperty("scaling", scaling.getId());
        json.addProperty("exhaustion", exhaustion);
        json.addProperty("effects", effects.getId());
        json.addProperty("death_message_type", deathMessageType.getId());
        return json;
    }

    public enum DamageScaling {
        NEVER("never"),
        ALWAYS("always"),
        WHEN_CAUSED_BY_LIVING_NON_PLAYER("when_caused_by_living_non_player");

        private final String id;

        DamageScaling(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum DamageEffects {
        HURT("hurt"),
        THORNS("thorns"),
        DROWNING("drowning"),
        BURNING("burning"),
        FREEZING("freezing"),
        POKING("poking");

        private final String id;

        DamageEffects(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum DeathMessageType {
        DEFAULT("default"),
        FALL_VARIANTS("fall_variants"),
        INTENTIONAL_GAME_DESIGN("intentional_game_design");

        private final String id;

        DeathMessageType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}