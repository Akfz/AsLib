package n.paradox.aslib.datagen.api;

import com.google.gson.JsonElement;
import net.minecraft.util.Identifier;

//Класс для генерации файла и пути
public abstract class DataSerializable {
    private final Identifier path;

    public DataSerializable(Identifier path) {
        this.path = path;
    }

    public Identifier getPath() {
        return this.path;
    }

    // реализация сериализации
    public abstract JsonElement serialize();
}
