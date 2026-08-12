package v.akfz.aslib.resourcepack.configpack;

import net.minecraft.server.packs.repository.Pack;

import java.util.List;

public class ConfigPackData implements ConfigData {
    public String name;
    public String id;
    public boolean alwaysEnabled;
    public List<String> description;
    public boolean pinned;
    public Pack.Position position;
    public String type = "default";

    // "client", "server" или "both" (по умолчанию "both")
    public String packTarget = "both";
}