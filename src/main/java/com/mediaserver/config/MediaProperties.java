package com.mediaserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "media")
@Data
public class MediaProperties {
    private Storage storage = new Storage();
    private Mega mega = new Mega();
    private Streaming streaming = new Streaming();

    @Data
    public static class Storage {
        private String path = "/var/media/videos";
        private String tempPath = "/var/media/temp";
        private int maxCacheSizeGb = 100;
    }

    @Data
    public static class Mega {
        private String email;
        private String password;
    }

    @Data
    public static class Streaming {
        private int chunkSize = 1048576; // 1MB
    }
}
