package v.akfz.aslib.util.json;

import java.nio.file.Path;

/**
 * Save-load json file
 * @param <T> class implementation jsonData with datas
 * <p>
 * WARN : will not updated
 * Use {@link v.akfz.aslib.util.af.BinaryHelper}
 */
public interface JsonFile<T extends JsonData> {
    T data();

    Path getPath();
}
