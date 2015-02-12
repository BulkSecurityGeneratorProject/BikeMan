package de.rwth.idsg.bikeman.ixsi.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.rwth.idsg.bikeman.ixsi.api.SubscriptionStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 04.11.2014
 */
public abstract class AbstractSubscriptionStore<T> implements SubscriptionStore<T> {

    // Want to get the logger of the extending class and not of this abstract one
    private final Logger log = LoggerFactory.getLogger(getClass());

    final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat(getClass().getSimpleName() + "-Thread-%d")
            .setDaemon(true)
            .build();

    /**
     * Key   (T)           = ID of the item
     * Value (Set<String>) = IDs of the subscribed systems
     */
    ConcurrentHashMap<T, Set<String>> lookupTable;

    /**
     * Service to remove expired subscriptions
     */
    ScheduledExecutorService scheduler;

    @Override
    public void subscribe(String systemID, List<T> itemIDs, Integer expireIntervalinMinutes) {
        subscribeInternal(systemID, itemIDs);
        scheduleRemove(systemID, itemIDs, expireIntervalinMinutes);
        log.debug("System '{}' subscribed to '{}'. This subscription is scheduled to expire in {} minutes",
                systemID, itemIDs, expireIntervalinMinutes);
    }

    @Override
    public void subscribe(String systemID, List<T> itemIDs) {
        subscribeInternal(systemID, itemIDs);
        log.debug("System '{}' subscribed to '{}'", systemID, itemIDs);
    }

    private void subscribeInternal(String systemID, List<T> itemIDs) {
        Set<String> systemIDSet;
        for (T itemID : itemIDs) {
            systemIDSet = lookupTable.get(itemID);

            if (systemIDSet == null) {
                systemIDSet = new HashSet<>();
                systemIDSet.add(systemID);
                lookupTable.put(itemID, systemIDSet);

            } else {
                systemIDSet.add(systemID);
            }
        }
    }

    @Override
    public void unsubscribe(String systemID, List<T> itemIDs) {
        Set<String> systemIDSet;
        for (T itemID : itemIDs) {
            systemIDSet = lookupTable.get(itemID);

            if (systemIDSet != null) {
                systemIDSet.remove(systemID);
            }
        }
        log.debug("System '{}' unsubscribed from '{}'", systemID, itemIDs);
    }

    @Override
    public Set<String> getSubscribedSystems(T itemID) {
        return lookupTable.get(itemID);
    }

    @Override
    public List<T> getSubscriptions(String systemID) {
        List<T> subscriptions = new ArrayList<>();
        for (Map.Entry<T, Set<String>> entry : lookupTable.entrySet()) {
            if (entry.getValue().contains(systemID)) {
                subscriptions.add(entry.getKey());
            }
        }
        return subscriptions;
    }

    @Override
    public void clear() {
        lookupTable.clear();
        log.debug("Cleared the subscription store");
    }

    @Override
    public String toString() {
        return lookupTable.toString();
    }

    // -------------------------------------------------------------------------
    // Schedule to remove subscriptions
    // -------------------------------------------------------------------------

    private void scheduleRemove(String systemID, List<T> itemIDs, long expireIntervalinMinutes) {
        scheduler.schedule(new RemoveJob(systemID, itemIDs), expireIntervalinMinutes, TimeUnit.MINUTES);
    }

    @RequiredArgsConstructor
    private class RemoveJob implements Runnable {

        private final String systemID;
        private final List<T> itemIDs;

        @Override
        public void run() {
            unsubscribe(systemID, itemIDs);
        }
    }
}