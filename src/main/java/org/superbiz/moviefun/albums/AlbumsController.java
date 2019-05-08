package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static org.apache.tika.io.IOUtils.*;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(BlobStore blobStore, AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

        String name = findCoverName(albumId);
        InputStream inputStream = uploadedFile.getInputStream();

        blobStore.put(new Blob(name, inputStream, uploadedFile.getContentType()));

        return format("redirect:/albums/%d", albumId);
    }


    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        Optional<Blob> optionalBlob = blobStore.get(findCoverName(albumId));

        Blob blob = optionalBlob.orElseGet(this::buildDefaultCover);

        final byte[] imageBytes = toByteArray(blob.inputStream);
        String contentType = new Tika().detect(imageBytes);

        return new HttpEntity<>(imageBytes, buildHttpHeaders(imageBytes, contentType));
    }

    private Blob buildDefaultCover() {

        InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("default-cover.jpg");
        return new Blob("default-header", defaultImageStream, MediaType.IMAGE_JPEG_VALUE);
    }


    private String findCoverName(long albumId) {
        return format("covers/%s", albumId);
    }

    private HttpHeaders buildHttpHeaders(byte[] imageBytes, String contentType) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }
}
