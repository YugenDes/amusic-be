package it.polimi.amusic.utils;

import lombok.EqualsAndHashCode;
import org.springframework.core.io.ByteArrayResource;

/**
 * Classe di supporto per ByteArrayResource
 * Poiche ByteArrayResource non prevede il filename
 * Questa classe viene in supporto per questa mancanza
 */
@EqualsAndHashCode(callSuper = true)
public class FileByteArrayResource extends ByteArrayResource {

    private final String fileName;

    public FileByteArrayResource(String fileName, byte[] byteArray, String description) {
        super(byteArray, description);
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}

