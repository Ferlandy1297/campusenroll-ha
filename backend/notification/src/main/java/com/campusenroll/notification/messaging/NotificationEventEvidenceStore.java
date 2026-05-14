package com.campusenroll.notification.messaging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventEvidenceStore {

    private final CopyOnWriteArrayList<String> evidence = new CopyOnWriteArrayList<>();

    public void record(String entry) {
        evidence.add(entry);
    }

    public List<String> snapshot() {
        return List.copyOf(evidence);
    }
}
