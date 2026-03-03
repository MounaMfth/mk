package iscae.mr.app_donation.files;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads")
public class FileController {

  private final Path uploadRoot = Paths.get("uploads");

  @GetMapping("/{directory}/{fileName:.+}")
  public ResponseEntity<Resource> getFile(@PathVariable String directory, @PathVariable String fileName)
      throws Exception {
    Path file = uploadRoot.resolve(directory).resolve(fileName).normalize();
    if (!Files.exists(file))
      return ResponseEntity.notFound().build();

    String ct = Files.probeContentType(file);
    MediaType type = (ct != null) ? MediaType.parseMediaType(ct) : MediaType.APPLICATION_OCTET_STREAM;

    return ResponseEntity.ok()
        .contentType(type)
        .body(new UrlResource(file.toUri()));
  }

  // Fallback for direct files in uploads root if any
  @GetMapping("/{fileName:.+}")
  public ResponseEntity<Resource> getRootFile(@PathVariable String fileName) throws Exception {
    Path file = uploadRoot.resolve(fileName).normalize();
    if (!Files.exists(file))
      return ResponseEntity.notFound().build();

    String ct = Files.probeContentType(file);
    MediaType type = (ct != null) ? MediaType.parseMediaType(ct) : MediaType.APPLICATION_OCTET_STREAM;

    return ResponseEntity.ok()
        .contentType(type)
        .body(new UrlResource(file.toUri()));
  }
}
