package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

import static java.nio.file.Files.readAllBytes;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = new File(blob.name);
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(blob.inputStream, outputStream);
        }
    }


    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {

            File coverFile = new File(name);
            byte[] imageBytes = readAllBytes(coverFile.toPath());
            Blob blob = new Blob(name, new ByteArrayInputStream(imageBytes), new Tika().detect(imageBytes));

            return Optional.of(blob);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }
}
