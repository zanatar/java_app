package ru.tsystems.tchallenge.service.config;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.springframework.context.annotation.Profile;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile(value = {"dev", "sit"})
@PropertySource("classpath:application.yml")
public class EmbeddedMongoConfig {

    @Value("${tchallenge.proxy.host}")
    private String proxyHost;

    @Value("${tchallenge.proxy.port}")
    private String proxyPort;


    @Bean
    public IRuntimeConfig embeddedMongoRuntimeConfig() {
        final Command command = Command.MongoD;

        return new RuntimeConfigBuilder().defaults(command)
                .artifactStore(new ExtractedArtifactStoreBuilder().defaults(command)
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command).proxyFactory(
                                        new HttpProxyFactory(proxyHost, Integer.parseInt(proxyPort)))
                                .build()))
                .build();

    }

}
