package net.cyllene.hackerrank.downloader.exceptions;

public class ExitWithErrorException extends RuntimeException {

    public ExitWithErrorException(Throwable e) {
        super(e);
    }

    public ExitWithErrorException(String s) {
        super(s);
    }
}
