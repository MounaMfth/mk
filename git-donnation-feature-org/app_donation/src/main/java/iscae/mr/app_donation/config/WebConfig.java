// src/main/java/iscae/mr/app_donation/config/WebConfig.java
package iscae.mr.app_donation.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.io.IOException;
import java.nio.file.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final Path uploadRoot;

  public WebConfig(@Value("${app.upload.base-dir:uploads}") String baseDir) {
    this.uploadRoot = Paths.get(baseDir).toAbsolutePath().normalize();
  }

  @PostConstruct
  public void ensureUploadDir() throws IOException {
    Files.createDirectories(uploadRoot);
    Files.createDirectories(uploadRoot.resolve("logos"));
    Files.createDirectories(uploadRoot.resolve("activites"));
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = uploadRoot.toUri().toString();
    if (!location.endsWith("/")) location += "/";
    // Expose: http://localhost:8080/uploads/** → {baseDir}/**
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location)
            .setCachePeriod(3600);
  }
}
