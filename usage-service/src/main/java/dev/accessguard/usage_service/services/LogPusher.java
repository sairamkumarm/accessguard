package dev.accessguard.usage_service.services;

import models.UsageEvent;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogPusher {
    private final RestHighLevelClient restHighLevelClient;

    public LogPusher(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public void indexUsage(UsageEvent event){
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("tenant",event.getTenantName());
            map.put("eventType", event.getEventType());
            map.put("timestamp",event.getTimestamp());
            map.put("payload",event.getPayload());

            IndexRequest indexRequest = new IndexRequest("usage-logs").source(map);

            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Failed indexing: "+e);
        }
    }
}
