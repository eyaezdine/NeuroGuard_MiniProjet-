package com.neuroguard.consultationservice.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ZoomService {

    // Simulation de création d'une réunion Zoom
    public MeetingInfo createMeeting(String topic, LocalDateTime startTime, long durationMinutes) {
        // Dans la réalité, appel à l'API Zoom avec OAuth2
        String meetingId = UUID.randomUUID().toString();
        String joinUrl = "https://zoom.us/j/" + meetingId;  // simulé
        return new MeetingInfo(meetingId, joinUrl);
    }

    public static class MeetingInfo {
        private final String meetingId;
        private final String joinUrl;

        public MeetingInfo(String meetingId, String joinUrl) {
            this.meetingId = meetingId;
            this.joinUrl = joinUrl;
        }

        public String getMeetingId() { return meetingId; }
        public String getJoinUrl() { return joinUrl; }
    }
}