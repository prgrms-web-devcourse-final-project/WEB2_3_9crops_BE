package io.crops.warmletter.domain.report.event;

import java.util.Map;

public record AIResultReadyEvent(Long reportId, Map<String, String> result) {}