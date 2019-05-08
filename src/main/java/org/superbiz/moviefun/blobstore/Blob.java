package org.superbiz.moviefun.blobstore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Blob {
    public final String name;
    public final InputStream inputStream;
    public final String contentType;
    public final long length = 0;

    public Blob(String name, InputStream inputStream, String contentType) {
        this.name = name;
        this.inputStream = inputStream;
        this.contentType = contentType;
    }
}
