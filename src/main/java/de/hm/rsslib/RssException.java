package de.hm.rsslib;

public class RssException extends Exception {

    public RssException(String message) {
        super(message);
    }

    public RssException(String message, Throwable cause) {
        super(message, cause);
    }

    public RssException(Throwable cause) {
        super(cause);
    }
}
