package v.akfz.aslib.test;

public class Test {
    /*
    public static void main(String[] args) {
        DataProvider test = new DataProvider() {
            @Override
            protected void registerDataSerializable() {

                SoundData data = new SoundData("test");

                data.addEntry(new SoundDataEntry(
                        new Identifier("aslib", "test_sound"),
                        Optional.of("subtitles.aslib.test_sound"),
                        List.of(new Identifier("aslib", "test_file_1"), new Identifier("aslib", "test_file_2"))
                ));

                add(data);
            }
        };
        test.run();
    }
     */
/*
    public static void main(String[] args) {
        DataProvider blockStateProvider = new DataProvider() {
            @Override
            protected void registerDataSerializable() {
                String modId = "avc";

                Identifier simpleBlock = new Identifier(modId, "test_block");
                Identifier simpleModel = new Identifier(modId, "block/model");

                BlockStateData simpleState = new BlockStateData(simpleBlock)
                        .addVariant(new SimpleBlockStateVariant(simpleModel));

                add(simpleState);


                Identifier furnaceBlock = new Identifier(modId, "super_furnace");
                Identifier furnaceModel = new Identifier(modId, "block/super_furnace");

                BlockStateData furnaceState = new BlockStateData(furnaceBlock)
                        .addVariant(new RotatableBlockStateVariant("facing=north", furnaceModel, 0))
                        .addVariant(new RotatableBlockStateVariant("facing=east",  furnaceModel, 90))
                        .addVariant(new RotatableBlockStateVariant("facing=south", furnaceModel, 180))
                        .addVariant(new RotatableBlockStateVariant("facing=west",  furnaceModel, 270));

                add(furnaceState);


                Identifier pillarBlock = new Identifier(modId, "magic_log");
                Identifier pillarModel = new Identifier(modId, "block/magic_log");

                BlockStateData pillarState = new BlockStateData(pillarBlock)
                        .addVariant(new RotatableBlockStateVariant("axis=y", pillarModel, 0, 0, false))
                        .addVariant(new RotatableBlockStateVariant("axis=x", pillarModel, 90, 90, true))
                        .addVariant(new RotatableBlockStateVariant("axis=z", pillarModel, 90, 0, true));

                add(pillarState);


                Identifier customBlock = new Identifier(modId, "random_grass");
                JsonObject customJson = new JsonObject();
                customJson.addProperty("model", "avc:block/grass_variant_1");
                customJson.addProperty("weight", 3);

                BlockStateData customState = new BlockStateData(customBlock)
                        .addVariant(new CustomBlockStateVariant("variant=type_1", customJson));

                add(customState);
            }
        };

        blockStateProvider.run();
    }
 */
}
