package it.polimi.amusic;

import it.polimi.amusic.external.gcs.FileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Files;

@SpringBootTest
@Slf4j
class GoogleCloudStroageTest {

    @Autowired
    FileService fileService;

    @Value("classpath:baseImageProfile.jpg")
    Resource image;

    @Test
    void eliminaFileTest() {
        final boolean b = fileService.deleteFile("https://storage.googleapis.com/download/storage/v1/b/polimi-amusic.appspot.com/o/ceb7392a-63a5-42bf-a9fa-109bbd284272?generation=1638382033633720&alt=media");
        Assertions.assertTrue(b, "Il File non é stato eliminato");
    }

    @Test
    void uploadFileTest() {
        final String mediaLink = fileService.uploadFile(image);
        System.out.println(mediaLink);
        Assertions.assertFalse(mediaLink.isEmpty(), "Il File non é stato caricato");
    }

    @SneakyThrows
    @Test
    void uploadFolder() {
        File dir = new File("C:\\Users\\andrea.messina\\Desktop\\images");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                final byte[] bytes = Files.readAllBytes(child.toPath());
                final String mediaLink = fileService.uploadFile(new ByteArrayResource(bytes));
                System.out.print("Filename " + child.getName() + "  ");
                System.out.println(mediaLink);
                Assertions.assertFalse(mediaLink.isEmpty(), "Il File non é stato caricato");
            }
        }
    }
}
