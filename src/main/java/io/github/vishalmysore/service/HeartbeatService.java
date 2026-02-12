package io.github.vishalmysore.service;

import io.github.vishalmysore.client.MoltbookClient;
import io.github.vishalmysore.config.MoltbookConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Legacy HeartbeatService - retained for backwards compatibility
 * The actual heartbeat logic has been moved to MoltbookHeartbeat in the client package
 * 
 * @deprecated Use io.github.moltbook.client.MoltbookHeartbeat instead
 */
@Service
@Slf4j
@Deprecated
public class HeartbeatService {

    private final MoltbookClient moltbookClient;
    private final MoltbookConfig config;

    public HeartbeatService(MoltbookClient moltbookClient, MoltbookConfig config) {
        this.moltbookClient = moltbookClient;
        this.config = config;
        log.info("HeartbeatService initialized - heartbeat logic is in MoltbookHeartbeat");
    }

    /**
     * @deprecated The heartbeat is now handled by MoltbookHeartbeat with @Scheduled annotation
     */
    @Deprecated
    public void heartbeat() {
        log.warn("This method is deprecated. Heartbeat runs automatically via MoltbookHeartbeat");
    }
}
