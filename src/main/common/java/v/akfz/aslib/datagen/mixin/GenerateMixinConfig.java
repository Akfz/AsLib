package v.akfz.aslib.datagen.mixin;

import v.akfz.aslib.datagen.api.DataProvider;

/**
 * DataProvider wrapper for {@link MixinConfigData}.
 * <p>
 * Usage example (from a {@code main} method or datagen entrypoint):
 * <pre>
 * new GenerateMixinConfig(
 *     new MixinConfigData()
 *         .packageName("smth.template_mod.mixin")
 *         .addAsLibPlugin()
 *         .addMixin("template_mod.mixin.TestMixin")
 * ).run("common");
 * </pre>
 */
public class GenerateMixinConfig extends DataProvider {

	private final MixinConfigData data;

	public GenerateMixinConfig(MixinConfigData data) {
		this.data = data;
	}

	@Override
	protected void registerDataSerializable() {
		add(data);
	}
}