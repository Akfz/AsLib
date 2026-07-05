package n.paradox.aslib.test;

import n.paradox.aslib.resourcepack.AddResourcePack;
import n.paradox.aslib.resourcepack.AsLibResourceResourceReloaderHelper;
import n.paradox.aslib.resourcepack.SimpleFileResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ResourcePackTest {

    public static void initializeTestPack(PackRepository packRepository, Path gameDir) {
        String modId = "aslib";
        String packName = "aslib_dynamic_pack";

        Path testPackRoot = gameDir.resolve("aslib_test_resources");
        try {
            Files.createDirectories(testPackRoot);

            Path sampleFile = testPackRoot.resolve("test_config.txt");
            Files.writeString(sampleFile, "Hello from ASLib virtual filesystem!", StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.err.println("[ASLib Test] Не удалось подготовить файлы для теста: " + e.getMessage());
            return;
        }

        SimpleFileResourcePack testPack = new SimpleFileResourcePack(packName, testPackRoot, modId);

        AsLibResourceResourceReloaderHelper.register(testPack, (manager) -> {
            System.out.println("[ASLib Test] Событие перезагрузки ресурсов! Начинаем чтение...");

            ResourceLocation testFileLocation = new ResourceLocation(modId, "test_config.txt");

            Optional<Resource> resourceOptional = manager.getResource(testFileLocation);

            if (resourceOptional.isPresent()) {
                Resource resource = resourceOptional.get();
                try (BufferedReader reader = resource.openAsReader()) {
                    String line = reader.readLine();
                    System.out.println("[ASLib Test] УСПЕШНО ПРОЧИТАНО из ресурс-пака: " + line);

                    Path outputLog = testPackRoot.resolve("reload_result.log");
                    String logMessage = "Processed input: '" + line + "' | Status: OK \n";

                    Files.writeString(outputLog, logMessage, StandardCharsets.UTF_8);
                    System.out.println("[ASLib Test] ДАННЫЕ СУКЦЕССИВНО ЗАПИСАНЫ в: " + outputLog.toAbsolutePath());

                } catch (IOException e) {
                    System.err.println("[ASLib Test] Ошибка чтения/записи во время обработки ресурса: " + e.getMessage());
                }
            } else {
                System.err.println("[ASLib Test] Файл " + testFileLocation + " не был найден в менеджере ресурсов!");
            }
        });

        AddResourcePack.addFRP(
                packRepository,
                testPack,
                Component.literal("Тестовый динамический пак от ASLib"),
                true,
                Pack.Position.TOP,
                false,
                PackSource.BUILT_IN
        );

        System.out.println("[ASLib Test] Тестовый Ресурс-пак успешно внедрен в репозиторий.");
    }
}