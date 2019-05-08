package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.lang.String.format;

public class S3Store implements BlobStore {

    private final AmazonS3Client s3Client;
    private final String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);

        byte[] bytes = IOUtils.toByteArray(blob.inputStream);
        metadata.setContentLength(bytes.length);

        // Create new object because reading blob's inputstream will advance its cursor
        blob = new Blob(blob.name, new ByteArrayInputStream(bytes), blob.contentType);
        PutObjectRequest request = new PutObjectRequest(photoStorageBucket, blob.name, blob.inputStream, metadata);
        s3Client.putObject(request);
    }

    private File getCoverFile(String albumId) {
        String coverFileName = format("covers/%s", albumId);
        return new File(coverFileName);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            GetObjectRequest request = new GetObjectRequest(photoStorageBucket, name);
            S3Object s3Object = s3Client.getObject(request);
            InputStream s3is = s3Object.getObjectContent();

            ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
            String contentType = objectMetadata.getContentType();

            Blob blob = new Blob(name, s3is, contentType);

            return Optional.of(blob);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }
}
