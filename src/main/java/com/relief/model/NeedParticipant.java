package com.relief.model;

import java.sql.Timestamp;

public class NeedParticipant {
    private int participantId;
    private int needId;
    private int userId;
    private Timestamp joinedAt;
    private Timestamp leftAt;
    private String participationStatus; // ACTIVE or WITHDRAWN

    public NeedParticipant() {
    }

    public NeedParticipant(int participantId, int needId, int userId, Timestamp joinedAt, Timestamp leftAt, String participationStatus) {
        this.participantId = participantId;
        this.needId = needId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.participationStatus = participationStatus;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getNeedId() {
        return needId;
    }

    public void setNeedId(int needId) {
        this.needId = needId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Timestamp getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Timestamp leftAt) {
        this.leftAt = leftAt;
    }

    public String getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(String participationStatus) {
        this.participationStatus = participationStatus;
    }

    public boolean isActiveDuring(Timestamp timestamp) {
        if (timestamp == null) {
            return "ACTIVE".equalsIgnoreCase(participationStatus);
        }
        if (joinedAt != null && timestamp.before(joinedAt)) {
            return false;
        }
        if (leftAt != null && timestamp.after(leftAt)) {
            return false;
        }
        return true;
    }
}
