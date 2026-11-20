package com.relief.service;

import com.relief.dao.AlertDAO;
import com.relief.dao.MessageDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.NeedParticipantDAO;
import com.relief.dao.PledgeDAO;
import com.relief.dao.UserDAO;
import com.relief.model.Alert;
import com.relief.model.Message;
import com.relief.model.Need;
import com.relief.model.Pledge;
import com.relief.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CommunicationService {

    private final MessageDAO messageDAO;
    private final AlertDAO alertDAO;
    private final NeedDAO needDAO;
    private final PledgeDAO pledgeDAO;
    private final UserDAO userDAO;
    private final NeedParticipantDAO needParticipantDAO;

    @Autowired
    public CommunicationService(MessageDAO messageDAO,
            AlertDAO alertDAO,
            NeedDAO needDAO,
            PledgeDAO pledgeDAO,
            UserDAO userDAO,
            NeedParticipantDAO needParticipantDAO) {
        this.messageDAO = messageDAO;
        this.alertDAO = alertDAO;
        this.needDAO = needDAO;
        this.pledgeDAO = pledgeDAO;
        this.userDAO = userDAO;
        this.needParticipantDAO = needParticipantDAO;
    }

    public CommunicationService(MessageDAO messageDAO,
            AlertDAO alertDAO,
            NeedDAO needDAO,
            PledgeDAO pledgeDAO,
            UserDAO userDAO) {
        this(messageDAO, alertDAO, needDAO, pledgeDAO, userDAO, null);
    }

    public CommunicationService(MessageDAO messageDAO,
            AlertDAO alertDAO,
            NeedDAO needDAO,
            PledgeDAO pledgeDAO) {
        this(messageDAO, alertDAO, needDAO, pledgeDAO, null, null);
    }

    /**
     * Checks whether a user has permission to participate in or view private conversations
     * for a relief need.
     * - Department Officers have district-wide coordination oversight.
     * - Affected Persons can access threads for their own needs.
     * - Donors can access threads ONLY if they have pledged, have a participation history,
     *   or are direct participants in the message thread.
     *   Browsing a public VERIFIED need does NOT grant conversation access.
     */
    public boolean isUserAuthorizedForNeedConversation(User user, int needId) {
        if (user == null) {
            return false;
        }

        Need need = needDAO.findById(needId);
        if (need == null) {
            return false;
        }

        String role = user.getRole();

        if ("DEPARTMENT".equalsIgnoreCase(role)) {
            return true;
        }

        if ("AFFECTED_PERSON".equalsIgnoreCase(role)) {
            return need.getCreatedBy() == user.getUserId();
        }

        if ("DONOR".equalsIgnoreCase(role)) {
            int donorId = user.getUserId();

            // Check if donor has pledged on this need
            List<Pledge> pledges = pledgeDAO.findByNeedId(needId);
            boolean hasPledged = pledges.stream().anyMatch(p -> p.getDonorId() == donorId);
            if (hasPledged) {
                return true;
            }

            // Check need_participants table
            if (needParticipantDAO != null && needParticipantDAO.hasAnyParticipation(needId, donorId)) {
                return true;
            }

            // Check if donor is already part of the message thread
            List<Message> existing = messageDAO.findByNeedId(needId);
            return existing.stream().anyMatch(m -> m.getSenderId() == donorId ||
                    (m.getReceiverId() != null && m.getReceiverId() == donorId));
        }

        return false;
    }

    public boolean isUserAuthorizedForNeed(User user, int needId) {
        return isUserAuthorizedForNeedConversation(user, needId);
    }

    public void sendNeedMessage(int needId, int senderId, Integer receiverId, String content) {
        sendNeedMessage(needId, senderId, receiverId, null, content);
    }

    public void sendNeedMessage(int needId, int senderId, Integer receiverId, String messageType, String content) {
        User sender = null;
        if (userDAO != null) {
            sender = userDAO.findById(senderId);
        }
        if (sender == null) {
            sender = new User();
            sender.setUserId(senderId);
            Need n = needDAO.findById(needId);
            if (n != null && n.getCreatedBy() == senderId) {
                sender.setRole("AFFECTED_PERSON");
            } else {
                sender.setRole("DONOR");
            }
        }
        sendNeedMessage(needId, sender, receiverId, messageType, content);
    }

    public void sendNeedMessage(int needId, User sender, Integer receiverId, String content) {
        sendNeedMessage(needId, sender, receiverId, null, content);
    }

    /**
     * Sends a message on a relief need thread with strict role-based receiver authorization.
     * Enforces two message types:
     * - DIRECT: specific receiverId != null
     * - BROADCAST: receiverId == null (visible to eligible participants of the need)
     */
    public void sendNeedMessage(int needId, User sender, Integer receiverId, String messageType, String content) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender is required.");
        }
        Need need = needDAO.findById(needId);
        if (need == null) {
            throw new IllegalArgumentException("Relief need #" + needId + " not found.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        if (!isUserAuthorizedForNeedConversation(sender, needId)) {
            throw new IllegalArgumentException(
                    "User #" + sender.getUserId() + " is not authorized to message on this relief need.");
        }

        // Validate explicitly specified receiver
        if (receiverId != null && receiverId > 0) {
            if (receiverId.equals(sender.getUserId())) {
                throw new IllegalArgumentException("Cannot send a message to yourself.");
            }
            if (userDAO != null) {
                User targetUser = userDAO.findById(receiverId);
                if (targetUser == null) {
                    throw new IllegalArgumentException("Receiver user #" + receiverId + " does not exist.");
                }
            }
        }

        Integer resolvedReceiverId = null;
        String resolvedMessageType = messageType != null ? messageType.trim().toUpperCase() : null;
        String role = sender.getRole() != null ? sender.getRole() : "";

        if ("DONOR".equalsIgnoreCase(role)) {
            // Donors can send DIRECT messages to the affected person associated with this need
            if ("BROADCAST".equalsIgnoreCase(resolvedMessageType)) {
                throw new IllegalArgumentException("Donors cannot send broadcast messages.");
            }
            int affectedPersonId = need.getCreatedBy();
            if (receiverId != null && receiverId > 0 && receiverId.intValue() != affectedPersonId) {
                throw new IllegalArgumentException(
                        "Donors can only send direct messages to the affected person associated with this need.");
            }
            resolvedReceiverId = affectedPersonId;
            resolvedMessageType = "DIRECT";

        } else if ("AFFECTED_PERSON".equalsIgnoreCase(role)) {
            // Collect authorized participants: donors who pledged, donors who messaged, or department officers
            Set<Integer> authorizedDonorIds = new LinkedHashSet<>();
            List<Pledge> pledges = pledgeDAO.findByNeedId(needId);
            if (pledges != null) {
                for (Pledge p : pledges) {
                    if (!"CANCELLED".equalsIgnoreCase(p.getStatus())) {
                        authorizedDonorIds.add(p.getDonorId());
                    }
                }
            }
            if (needParticipantDAO != null) {
                needParticipantDAO.findActiveByNeed(needId).forEach(np -> authorizedDonorIds.add(np.getUserId()));
            }
            List<Message> threadMessages = messageDAO.findByNeedId(needId);
            if (threadMessages != null) {
                for (Message m : threadMessages) {
                    if (m.getSenderId() != sender.getUserId()) {
                        authorizedDonorIds.add(m.getSenderId());
                    }
                }
            }

            if (receiverId != null && receiverId > 0) {
                boolean isDepartment = false;
                if (userDAO != null) {
                    User rec = userDAO.findById(receiverId);
                    if (rec != null && "DEPARTMENT".equalsIgnoreCase(rec.getRole())) {
                        isDepartment = true;
                    }
                }
                if (!authorizedDonorIds.contains(receiverId) && !isDepartment) {
                    throw new IllegalArgumentException(
                            "Recipient #" + receiverId + " is not an authorized participant or pledged donor for this need.");
                }
                resolvedReceiverId = receiverId;
                resolvedMessageType = "DIRECT";
            } else {
                // No receiver specified: If explicit DIRECT was requested without receiver, fail
                if ("DIRECT".equalsIgnoreCase(resolvedMessageType)) {
                    if (authorizedDonorIds.size() == 1) {
                        resolvedReceiverId = authorizedDonorIds.iterator().next();
                    } else {
                        throw new IllegalArgumentException("A direct message requires an explicit receiver ID.");
                    }
                } else {
                    // Send as BROADCAST to all eligible need participants
                    resolvedReceiverId = null;
                    resolvedMessageType = "BROADCAST";
                }
            }

        } else if ("DEPARTMENT".equalsIgnoreCase(role)) {
            // Department officers have oversight
            if (receiverId != null && receiverId > 0) {
                resolvedReceiverId = receiverId;
                resolvedMessageType = "DIRECT";
            } else {
                resolvedReceiverId = null;
                resolvedMessageType = "BROADCAST";
            }
        } else {
            throw new IllegalArgumentException("User role '" + role + "' is not authorized to send messages.");
        }

        // Validate final contract
        if ("DIRECT".equalsIgnoreCase(resolvedMessageType) && resolvedReceiverId == null) {
            throw new IllegalArgumentException("Direct messages must have a non-null receiver ID.");
        }
        if ("BROADCAST".equalsIgnoreCase(resolvedMessageType) && resolvedReceiverId != null) {
            throw new IllegalArgumentException("Broadcast messages must have receiver_id = null.");
        }

        Message msg = new Message();
        msg.setNeedId(needId);
        msg.setSenderId(sender.getUserId());
        msg.setReceiverId(resolvedReceiverId);
        msg.setContent(content.trim());
        msg.setMessageType(resolvedMessageType);

        messageDAO.save(msg);
    }

    /**
     * Retrieves messages for a need, scoped to the viewing user's role and participation history:
     * - Department: Sees full conversation.
     * - Affected Person: Sees direct messages addressed to or from them, and all broadcasts for their need.
     * - Donor: Sees their direct messages, and broadcasts ONLY from their active participation periods.
     *   Unpledged/withdrawn donors cannot see broadcasts outside their participation window.
     */
    public List<Message> getMessagesForNeedAndUser(int needId, User user) {
        if (user == null || !isUserAuthorizedForNeedConversation(user, needId)) {
            return Collections.emptyList();
        }

        List<Message> all = messageDAO.findByNeedId(needId);

        // Department officers and the owning affected person see all thread updates
        if ("DEPARTMENT".equalsIgnoreCase(user.getRole()) || "AFFECTED_PERSON".equalsIgnoreCase(user.getRole())) {
            return all;
        }

        // Donors see their own direct messages and broadcasts from their participation periods
        int donorId = user.getUserId();

        return all.stream().filter(m -> {
            String type = m.getMessageType() != null ? m.getMessageType() : "DIRECT";
            if ("DIRECT".equalsIgnoreCase(type)) {
                return m.getSenderId() == donorId || (m.getReceiverId() != null && m.getReceiverId() == donorId);
            }
            if ("BROADCAST".equalsIgnoreCase(type)) {
                if (needParticipantDAO != null) {
                    return needParticipantDAO.isUserParticipantAt(needId, donorId, m.getSentAt());
                }
                // Fallback for mock test environments without needParticipantDAO
                List<Pledge> pledges = pledgeDAO.findByNeedId(needId);
                return pledges.stream().anyMatch(p -> p.getDonorId() == donorId && !"CANCELLED".equalsIgnoreCase(p.getStatus()));
            }
            return false;
        }).toList();
    }

    public List<Message> getMessagesForNeed(int needId) {
        return messageDAO.findByNeedId(needId);
    }

    public void broadcastAlert(int disasterId,
            int departmentUserId,
            String title,
            String messageText,
            double latitude,
            double longitude,
            double radiusKm) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert title is required.");
        }
        if (messageText == null || messageText.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert message is required.");
        }
        if (radiusKm <= 0) {
            radiusKm = 10.0;
        }

        Alert alert = new Alert();
        alert.setDisasterId(disasterId);
        alert.setCreatedBy(departmentUserId);
        alert.setTitle(title.trim());
        alert.setMessage(messageText.trim());
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        alert.setRadiusKm(radiusKm);

        alertDAO.save(alert);
    }

    public List<Alert> getAllAlerts() {
        return alertDAO.findAll();
    }

    public List<Alert> getRelevantAlertsForUser(User user) {
        List<Alert> all = alertDAO.findAll();
        if (user == null || "DEPARTMENT".equalsIgnoreCase(user.getRole())) {
            return all;
        }
        List<Alert> relevant = new ArrayList<>();
        for (Alert alert : all) {
            double dist = PledgeAllocationService.calculateHaversineKm(
                    user.getLatitude(),
                    user.getLongitude(),
                    alert.getLatitude(),
                    alert.getLongitude());
            if (dist <= alert.getRadiusKm()) {
                relevant.add(alert);
            }
        }
        return relevant;
    }
}
