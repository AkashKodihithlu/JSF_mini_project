package com.relief.service;

import com.relief.dao.AlertDAO;
import com.relief.dao.MessageDAO;
import com.relief.dao.NeedDAO;
import com.relief.model.Alert;
import com.relief.model.Message;
import com.relief.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommunicationService {

    private final MessageDAO messageDAO;
    private final AlertDAO alertDAO;
    private final NeedDAO needDAO;

    public CommunicationService(MessageDAO messageDAO,
            AlertDAO alertDAO,
            NeedDAO needDAO) {
        this.messageDAO = messageDAO;
        this.alertDAO = alertDAO;
        this.needDAO = needDAO;
    }

    public void sendNeedMessage(int needId, int senderId, Integer receiverId, String content) {
        if (needDAO.findById(needId) == null) {
            throw new IllegalArgumentException("Relief need #" + needId + " not found.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        Message msg = new Message();
        msg.setNeedId(needId);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content.trim());
        messageDAO.save(msg);
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
                    user.getLatitude(), user.getLongitude(),
                    alert.getLatitude(), alert.getLongitude());
            if (dist <= alert.getRadiusKm()) {
                relevant.add(alert);
            }
        }
        return relevant;
    }
}