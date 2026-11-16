
package com.relief.service;

import com.relief.dao.AlertDAO;
import com.relief.dao.MessageDAO;
import com.relief.dao.NeedDAO;
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

    @Autowired
    public CommunicationService(MessageDAO messageDAO,
            AlertDAO alertDAO,
            NeedDAO needDAO,
            PledgeDAO pledgeDAO,
            UserDAO userDAO) {
        this.messageDAO = messageDAO;
        this.alertDAO = alertDAO;
        this.needDAO = needDAO;
        this.pledgeDAO = pledgeDAO;
        this.userDAO = userDAO;
    }

    public CommunicationService(MessageDAO messageDAO,
            AlertDAO alertDAO,
            NeedDAO needDAO,
            PledgeDAO pledgeDAO) {
        this(messageDAO, alertDAO, needDAO, pledgeDAO, null);
    }

    /**
     * Checks whether a user is authorized to view and participate
     * in a relief need's conversation.
     */
    public boolean isUserAuthorizedForNeed(User user, int needId) {
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

            // Check if donor has pledged on this need
            List<Pledge> pledges = pledgeDAO.findByNeedId(needId);

            boolean hasPledged = pledges.stream()
                    .anyMatch(p -> p.getDonorId() == user.getUserId());

            if (hasPledged) {
                return true;
            }

            // Check if donor is already part of the message thread
            List<Message> existing = messageDAO.findByNeedId(needId);

            boolean inMessages = existing.stream().anyMatch(m -> m.getSenderId() == user.getUserId() ||
                    (m.getReceiverId() != null &&
                            m.getReceiverId() == user.getUserId()));

            if (inMessages) {
                return true;
            }

            // Check if need is verified and available in donor matching
            String status = need.getStatus();

            return "VERIFIED".equalsIgnoreCase(status)
                    || "PARTIALLY_FULFILLED".equalsIgnoreCase(status);
        }

        return false;
    }

    /**
     * Sends a message on a relief need thread.
     * Resolves sender and delegates to role-aware validation.
     */
    public void sendNeedMessage(int needId, int senderId,
            Integer receiverId, String content) {

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

        sendNeedMessage(needId, sender, receiverId, content);
    }

    /**
     * Sends a message with role-based receiver authorization.
     */
    public void sendNeedMessage(int needId, User sender,
            Integer receiverId, String content) {

        if (sender == null) {
            throw new IllegalArgumentException("Sender is required.");
        }

        Need need = needDAO.findById(needId);

        if (need == null) {
            throw new IllegalArgumentException(
                    "Relief need #" + needId + " not found.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        if (!isUserAuthorizedForNeed(sender, needId)) {
            throw new IllegalArgumentException(
                    "User #" + sender.getUserId()
                            + " is not authorized to message on this relief need.");
        }

        // Validate explicitly specified receiver
        if (receiverId != null && receiverId > 0) {

            if (receiverId.equals(sender.getUserId())) {
                throw new IllegalArgumentException(
                        "Cannot send a message to yourself.");
            }

            if (userDAO != null) {
                User targetUser = userDAO.findById(receiverId);

                if (targetUser == null) {
                    throw new IllegalArgumentException(
                            "Receiver user #" + receiverId + " does not exist.");
                }
            }
        }

        Integer resolvedReceiverId = null;

        String role = sender.getRole() != null
                ? sender.getRole()
                : "";

        if ("DONOR".equalsIgnoreCase(role)) {

            // Donors can message only the affected person associated with this need
            int affectedPersonId = need.getCreatedBy();

            if (receiverId != null && receiverId > 0
                    && receiverId.intValue() != affectedPersonId) {

                throw new IllegalArgumentException(
                        "Donors can only send messages to the affected person associated with this need.");
            }

            resolvedReceiverId = affectedPersonId;

        } else if ("AFFECTED_PERSON".equalsIgnoreCase(role)) {

            // Collect authorized participants
            Set<Integer> authorizedDonorIds = new LinkedHashSet<>();

            List<Pledge> pledges = pledgeDAO.findByNeedId(needId);

            if (pledges != null) {
                for (Pledge p : pledges) {
                    authorizedDonorIds.add(p.getDonorId());
                }
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

                    if (rec != null
                            && "DEPARTMENT".equalsIgnoreCase(rec.getRole())) {
                        isDepartment = true;
                    }
                }

                if (!authorizedDonorIds.contains(receiverId)
                        && !isDepartment) {

                    throw new IllegalArgumentException(
                            "Recipient #" + receiverId
                                    + " is not an authorized participant or pledged donor for this need.");
                }

                resolvedReceiverId = receiverId;

            } else {

                // Direct message when exactly one donor exists.
                // Broadcast when multiple donors exist.
                if (authorizedDonorIds.size() == 1) {
                    resolvedReceiverId = authorizedDonorIds.iterator().next();
                } else {
                    resolvedReceiverId = null;
                }
            }

        } else if ("DEPARTMENT".equalsIgnoreCase(role)) {

            // Department officers have oversight
            if (receiverId != null && receiverId > 0) {
                resolvedReceiverId = receiverId;
            } else {
                resolvedReceiverId = null;
            }

        } else {
            throw new IllegalArgumentException(
                    "User role '" + role
                            + "' is not authorized to send messages.");
        }

        Message msg = new Message();
        msg.setNeedId(needId);
        msg.setSenderId(sender.getUserId());
        msg.setReceiverId(resolvedReceiverId);
        msg.setContent(content.trim());

        messageDAO.save(msg);
    }

    /**
     * Retrieves messages for a need, scoped to the viewing user's conversation.
     */
    public List<Message> getMessagesForNeedAndUser(int needId, User user) {

        if (user == null) {
            return Collections.emptyList();
        }

        if (!isUserAuthorizedForNeed(user, needId)) {
            return Collections.emptyList();
        }

        List<Message> all = messageDAO.findByNeedId(needId);

        // Department officers and owning affected person see all updates
        if ("DEPARTMENT".equalsIgnoreCase(user.getRole())
                || "AFFECTED_PERSON".equalsIgnoreCase(user.getRole())) {
            return all;
        }

        // Donors see their messages and broadcast messages
        int donorId = user.getUserId();

        return all.stream().filter(m -> m.getSenderId() == donorId ||
                (m.getReceiverId() != null
                        && m.getReceiverId() == donorId)
                ||
                m.getReceiverId() == null).toList();
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

        if (user == null
                || "DEPARTMENT".equalsIgnoreCase(user.getRole())) {
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
