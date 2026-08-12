package v.akfz.aslib.resourcepack.configpack.preview;

import v.akfz.aslib.resourcepack.configpack.ConfigData;

import java.util.List;

public class PreviewConfig implements ConfigData {
    public String name = "change to name | замени для имени";
    public String id = "like mod id, for use in game id:path | как мод айди, для использования в игре id:путь";
    public String alwaysEnabled = "true/false - if true, pack cannot be turned off | true/false - если true, невозможно выключить";
    public List<String> description = List.of(
            "ENG : This file creates a resource pack in Minecraft with files inside this folder...",
            "RU : Этот файл создает в майнкрафте ресурс-пак с файлами внутри папки...",
            "\uD83D\uDE0F"
    );
    public String pinned = "true/false - can change pos in pack list (in minecraft settings) | true/false - можно ли менять позицию в пак-листе";
    public String position = "only TOP or BOTTOM, set in pack list | только TOP или BOTTOM, ставит в пак-листе либо самым первым (TOP), либо в конце (BOTTOM)";
    public String type = "pack type (e.g. 'default') | тип пака (например, 'default')";
}