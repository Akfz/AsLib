package v.akfz.aslib;

import v.akfz.aslib.annotation.DontCompile;
import v.akfz.aslib.datagen.fabric.mod.FabricModJsonData;
import v.akfz.aslib.datagen.fabric.mod.GenerateFabricModJson;
import v.akfz.aslib.datagen.forge.modstoml.GenerateModsToml;
import v.akfz.aslib.datagen.forge.modstoml.ModsTomlData;
import v.akfz.aslib.datagen.forge.packmcmeta.GeneratePackMcmeta;
import v.akfz.aslib.datagen.forge.packmcmeta.PackMcmetaData;

@DontCompile
public class AsLibForgeFabricDataGen {
    public static void main(String[] args) {
        new GenerateFabricModJson(new FabricModJsonData().mixin("aslib.mixins.json").entrypoint("v.akfz.aslib.AsLib_fabric")).run("common");
        new GenerateModsToml(new ModsTomlData()).run("common");
        new GeneratePackMcmeta(new PackMcmetaData()).run("common");
    }
}
