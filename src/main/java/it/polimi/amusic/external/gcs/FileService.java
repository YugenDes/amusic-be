package it.polimi.amusic.external.gcs;

import it.polimi.amusic.exception.GCPBucketException;
import org.springframework.core.io.Resource;

public interface FileService {

    String uploadFile(Resource file) throws GCPBucketException;

    Resource downloadFile(String fileName) throws GCPBucketException;

    boolean deleteFile(String fileName) throws GCPBucketException;

}
