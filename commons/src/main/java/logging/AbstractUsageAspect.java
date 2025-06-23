package logging;

import annotations.TrackUsage;
import models.UsageEvent;

import java.time.Instant;

public abstract class AbstractUsageAspect {
    protected abstract void emit(Object payload);
    public void process(TrackUsage ann, Object result){
        String tenant = TenantContext.getTenant();
        if (tenant == null){
            tenant = "unknown";
        }
        UsageEvent event = new UsageEvent(ann.eventType(), TenantContext.getTenant(), Instant.now(), result);
        emit(event);
    }
}
