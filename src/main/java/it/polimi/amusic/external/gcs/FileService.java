package it.polimi.amusic.external.gcs;

import it.polimi.amusic.exception.GCPBucketException;
import org.springframework.core.io.Resource;

public interface FileService {

    String uploadFile(Resource file) throws GCPBucketException;

    Resource downloadFile(String fileName) throws GCPBucketException;

    boolean deleteFile(String fileName) throws GCPBucketException;

    String BASE_USER_PHOTO_URL = "https://storage.googleapis.com/download/storage/v1/b/polimi-amusic.appspot.com/o/4040f9a7-2612-4392-926f-b56c78f33220?generation=1638967287738590&alt=media";

    String BASE_EVENT_PHOTO_URL = "https://storage.googleapis.com/download/storage/v1/b/polimi-amusic.appspot.com/o/0cf329b3-38c1-47dd-b361-ad20ac5ffefd?generation=1638820017396282&alt=media";

}
