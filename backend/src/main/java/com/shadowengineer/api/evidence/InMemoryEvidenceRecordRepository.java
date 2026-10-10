package com.shadowengineer.api.evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryEvidenceRecordRepository implements EvidenceRecordRepository {

    private final Map<UUID, List<EvidenceRecord>> recordsByConnection = new ConcurrentHashMap<>();

    @Override
    public void replaceForConnection(UUID connectionId, List<EvidenceRecord> records) {
        recordsByConnection.put(connectionId, List.copyOf(records));
    }

    @Override
    public List<EvidenceRecord> findByUserId(UUID userId) {
        return recordsByConnection.values().stream()
                .flatMap(List::stream)
                .filter(record -> record.userId().equals(userId))
                .toList();
    }
}
