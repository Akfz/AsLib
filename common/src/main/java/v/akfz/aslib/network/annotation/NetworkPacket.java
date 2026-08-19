package v.akfz.aslib.network.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a unique ResourceLocation identifier to a {@link v.akfz.aslib.network.api.Packet} class.
 * <p>
 * Example:
 * <pre>{@code
 * @NetworkPacket("modid:my_packet")
 * public class MyPacket implements Packet { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NetworkPacket {
    /**
     * @return String representation of the ResourceLocation (e.g., "modid:packet_id").
     */
    String value();
}