package it.polimi.amusic.external.gcs.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import it.polimi.amusic.exception.FilenameNotFoundInUrlException;
import it.polimi.amusic.exception.GCPBucketException;
import it.polimi.amusic.external.gcs.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final GoogleCredentials credentials;
    private static final String BUCKET_NAME = "polimi-amusic.appspot.com";
    private static final Pattern patternGCS = Pattern.compile("(polimi-amusic.appspot.com)(\\/)(.+)(\\/)(.+)(\\?)(.+\\=.+)");
    private static final Pattern patternUUID = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

    /**
     * Carica il file sul bucket di google cloud
     *
     * @param file file
     * @return String fileUrl
     * @throws GCPBucketException exception
     */
    //TODO aggiungre compressione immagine
    @Override
    public String uploadFile(Resource file) throws GCPBucketException {
        //Creo l'identificatore del blob
        //Utilizzo come name strategy la creazione di un UUID random
        BlobId blobId = BlobId.of(BUCKET_NAME, UUID.randomUUID().toString());
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
     *
     * @param url filename
     * @return Resource file
     * @throws GCPBucketException exception
     */
    @Override
    public Resource downloadFile(String url) throws GCPBucketException {
        //Creo l'identificatore del blob associato al filename
        final String filename = regexFilenameFromUrl(url);
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        final Storage service = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        //Recupero il blob
        final Blob blob = service.get(blobId);
        try {
            //Lo converto in un formato gestibile come Resource
            final byte[] content = blob.getContent();
            return new ByteArrayResource(content, filename);
        } catch (StorageException | NullPointerException e) {
            throw new GCPBucketException("Errore durante il download del file {}", e.getLocalizedMessage());
        }
    }

    /**
     * Cancella il file dal bucket di google cloud
     *
     * @param url url
     * @return boolean status dell'operazione
     * @throws GCPBucketException exception
     */
    @Override
    public boolean deleteFile(String url) throws GCPBucketException {
        final String filename = regexFilenameFromUrl(url);
        try {
            //Creo l'identificatore del blob associato al filename
            BlobId blobId = BlobId.of(BUCKET_NAME, filename);
            final Storage service = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
            //Elimino il file
            return service.delete(blobId);
        } catch (Exception e) {
            throw new GCPBucketException("Errore durante l'eliminazione del file {}", filename);
        }
    }


    private String regexFilenameFromUrl(String url) {
        if (!url.contains(BUCKET_NAME)) {
            throw new FilenameNotFoundInUrlException("Non é presente il bucket name all'interno dell url {}", url);
        }
        final String urlTrimmed = url.substring(url.indexOf(BUCKET_NAME));
        log.debug("parsing GCS URL to filename. URL {}", url);
        final Matcher matcherGCS = patternGCS.matcher(urlTrimmed);
        if (!matcherGCS.find()) {
            throw new FilenameNotFoundInUrlException("L'url non é nel formato corretto {}", url);
        }
        final String filename = matcherGCS.group(5);
        log.debug("Filename GCS {}", filename);

        final Matcher matcher = patternUUID.matcher(filename);
        if (!matcher.find()) {
            throw new FilenameNotFoundInUrlException("Il filename non é stato trovato all'interno dell url");
        }
        return filename;
    }
}
