package it.polimi.amusic.external.gcs.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import it.polimi.amusic.exception.GCPBucketException;
import it.polimi.amusic.external.gcs.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final GoogleCredentials credentials;

    /**
     * Carica il file sul bucket di google cloud
     * @param file
     * @return String fileUrl
     * @throws GCPBucketException
     */
    @Override
    public String uploadFile(Resource file) throws GCPBucketException {
        //Creo l'identificatore del blob
        //Utilizzo come name strategy la creazione di un UUID random
        BlobId blobId = BlobId.of("polimi-amusic.appspot.com", UUID.randomUUID().toString());
        //Creo le info associate al blob
        BlobInfo blobInfo = BlobInfo
                .newBuilder(blobId)
                //il valore media poiché vengono caricate solo immagini
                .setContentType("media")
                .build();
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        try {
            //Creo il blob , se non va in exception il file é stato salvato
            Blob blob = storage.create(blobInfo, file.getInputStream().readAllBytes());
            //restituisco il link del file visibile da broswer
            return blob.getMediaLink();
        } catch (IOException e) {
            throw new GCPBucketException("Errore durante l'upload del file {}", e.getLocalizedMessage());
        }
    }

    /**
     * Scarica il file dal bucket di google cloud
     * @param fileName
     * @return Resource file
     * @throws GCPBucketException
     */
    @Override
    public Resource downloadFile(String fileName) throws GCPBucketException {
        //Creo l'identificatore del blob associato al filename
        BlobId blobId = BlobId.of("polimi-amusic.appspot.com", fileName);
        final Storage service = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        //Recupero il blob
        final Blob blob = service.get(blobId);
        try {
            //Lo converto in un formato gestibile come Resource
            final byte[] content = blob.getContent();
            return new ByteArrayResource(content, fileName);
        } catch (StorageException | NullPointerException e) {
            throw new GCPBucketException("Errore durante il download del file {}", e.getLocalizedMessage());
        }
    }

    /**
     * Cancella il file dal bucket di google cloud
     * @param fileName
     * @return boolean status dell'operazione
     * @throws GCPBucketException
     */
    @Override
    public boolean deleteFile(String fileName) throws GCPBucketException {
        try {
            //Creo l'identificatore del blob associato al filename
            BlobId blobId = BlobId.of("polimi-amusic.appspot.com", fileName);
            final Storage service = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
            //Elimino il file
            final boolean delete = service.delete(blobId);
            //il metodo ritorna vero/falso a seconda se é riuscito a eliminare il blob
            return delete;
        } catch (Exception e) {
            throw new GCPBucketException("Errore durante l'eliminazione del file {}", fileName);
        }
    }
}
