package models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
public class UsageEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String eventType;
    private String tenantName;
    private Instant timestamp;
    private Object payload;

    public UsageEvent(){}

    @Override
    public String toString() {
        return "{" +
                "tenantName: " + tenantName + "\n" +
                "eventType: " + eventType + "\n" +
                "timestamp: " + timestamp + "\n" +
                "payload: " + (payload != null ? payload.toString() : "null") +
                "}";
    }
}
