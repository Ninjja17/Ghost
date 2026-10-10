package com.shadowengineer.api.evidence;

import java.util.List;
import java.util.UUID;

public interface EvidenceRecordRepository {

    void replaceForConnection(UUID connectionId, List<EvidenceRecord> records);

    List<EvidenceRecord> findByUserId(UUID userId);
}
