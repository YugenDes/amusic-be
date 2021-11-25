package it.polimi.amusic.utils;

import org.springframework.core.io.ByteArrayResource;

/**
 * Classe di supporto per ByteArrayResource
 * Poiche ByteArrayResource non prevede il filename
 * Questa classe viene in supporto per questa mancanza
 */
public class FileByteArrayResource extends ByteArrayResource {

    private String fileName;

    public FileByteArrayResource(String fileName, byte[] byteArray, String description) {
        super(byteArray, description);
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}

