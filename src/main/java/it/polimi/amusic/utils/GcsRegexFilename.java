package it.polimi.amusic.utils;

import it.polimi.amusic.exception.FilenameNotFoundInUrlException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GcsRegexFilename {

    private static final String BUCKET_NAME = "polimi-amusic.appspot.com";
    private static final Pattern patternGCS = Pattern.compile("(polimi-amusic.appspot.com)(\\/)(.+)(\\/)(.+)(\\?)(.+\\=.+)");
    private static final Pattern patternUUID = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

    public static boolean isFromGCS(String url) {
        return url.contains(BUCKET_NAME);
    }

    public static String getFilenameFromGcsUrl(String url) {
        if (!isFromGCS(url)) {
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
