package dev.accessguard.usage_service.config;

import models.UsageEvent;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class ElasticSearchConfig {
    private RestClient restClient;

    @Value("${bonsai.host}")
    private String host;

    @Value("${bonsai.port}")
    private int port;

    @Value("${bonsai.protocol}")
    private String protocol;

    @Value("${bonsai.username}")
    private String username;

    @Value("${bonsai.password}")
    private String password;

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        BasicCredentialsProvider creds = new BasicCredentialsProvider();
        creds.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return new RestHighLevelClient(org.elasticsearch.client.RestClient.builder(new HttpHost(host, port, protocol)).
                setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.
                        setDefaultCredentialsProvider(creds).setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));

    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsageEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UsageEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UsageEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

}
