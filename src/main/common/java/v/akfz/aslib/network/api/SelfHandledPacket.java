package v.akfz.aslib.network.api;

/**
 * Optional marker for {@link Packet}s that need a real {@link PacketHandler}
 * when registered via {@code @NetworkPacket(autoreg = true)}.
 * <p>
 * Packets that don't implement this get an empty handler from the
 * APT-generated registrar. Packets that do — get whatever
 * {@link #handler()} returns.
 * <p>
 * Example:
 * <pre>{@code
 * @NetworkPacket("mymod:hello")
 * public class HelloPacket implements Packet, SelfHandledPacket<HelloPacket> {
 *     @Override public PacketHandler<HelloPacket> handler() {
 *         return new MyHandler();
 *     }
 * }
 * }</pre>
 */
public interface SelfHandledPacket<T extends Packet> {
	PacketHandler<T> handler();
}