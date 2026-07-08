package v.akfz.aslib.util.json;

import java.nio.file.Path;

public interface JsonFile<T extends JsonData> {
    T data();

    Path getPath();
}
