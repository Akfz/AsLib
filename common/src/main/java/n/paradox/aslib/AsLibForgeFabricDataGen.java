package n.paradox.aslib;

import n.paradox.aslib.datagen.fabric.mod.FabricModJsonData;
import n.paradox.aslib.datagen.fabric.mod.GenerateFabricModJson;
import n.paradox.aslib.datagen.forge.modstoml.GenerateModsToml;
import n.paradox.aslib.datagen.forge.modstoml.ModsTomlData;
import n.paradox.aslib.datagen.forge.packmcmeta.GeneratePackMcmeta;
import n.paradox.aslib.datagen.forge.packmcmeta.PackMcmetaData;

public class AsLibForgeFabricDataGen {
    public static void main(String[] args) {
        new GenerateFabricModJson(new FabricModJsonData().mixin("aslib.mixins.json").entrypoint("n.paradox.aslib.fabric.ASLibFabricInitializer")).run("common");
        new GenerateModsToml(new ModsTomlData()).run("common");
        new GeneratePackMcmeta(new PackMcmetaData()).run("common");
    }
}
