package it.polimi.amusic.utils;

import org.apache.commons.lang3.RegExUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBuilder {

    public static String buildMessage(String message, Object... pars) {
        for (Object par : pars) {
            if (par != null) {
                message = RegExUtils.replaceFirst(message, Pattern.quote("{}"), Matcher.quoteReplacement(par.toString()));
            } else {
                message = RegExUtils.replaceFirst(message, Pattern.quote("{}"), Matcher.quoteReplacement(""));
            }
        }
        return message;
    }

}
