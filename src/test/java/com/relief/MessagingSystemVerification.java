package com.relief;

import com.relief.controller.AuthController;
import com.relief.controller.CommunicationController;
import com.relief.controller.PledgeAllocationController;
import com.relief.dao.AlertDAO;
import com.relief.dao.AllocationDAO;
import com.relief.dao.MessageDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.NeedParticipantDAO;
import com.relief.dao.PledgeDAO;
import com.relief.dao.UserDAO;
import com.relief.model.Allocation;
import com.relief.model.Message;
import com.relief.model.Need;
import com.relief.model.NeedParticipant;
import com.relief.model.Pledge;
import com.relief.model.User;
import com.relief.service.AuthService;
import com.relief.service.CommunicationService;
import com.relief.service.PledgeAllocationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessagingSystemVerification {

    private static HttpSession createMockSession(Map<String, Object> attributes) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if ("getAttribute".equals(methodName)) {
                        return attributes.get(methodArgs[0]);
                    } else if ("setAttribute".equals(methodName)) {
                        attributes.put((String) methodArgs[0], methodArgs[1]);
                        return null;
                    } else if ("removeAttribute".equals(methodName)) {
                        return attributes.remove(methodArgs[0]);
                    }
                    return null;
                }
        );
    }

    private static HttpServletRequest createMockRequest(HttpSession session, AtomicBoolean changeSessionIdCalled) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if ("getSession".equals(methodName)) {
                        return session;
                    } else if ("changeSessionId".equals(methodName)) {
                        if (changeSessionIdCalled != null) {
                            changeSessionIdCalled.set(true);
                        }
                        return "NEW_SESSION_ID";
                    }
                    return null;
                }
        );
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Assertion Failed: " + message + " [expected=" + expected + ", actual=" + actual + "]");
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("=== ReliefGrid Two-Way Messaging Verification Suite (13 Checks) ===");
        System.out.println("===============================================================");

        // Test User IDs (matching live data & test scenario)
        int affectedPersonId = 4; // Akash
        int donor1Id = 6;         // Suresh
        int donor2Id = 7;         // Ramesh
        int unpledgedDonorId = 8; // Mahesh (donor with no pledges on need #3)
        int unrelatedCitizenId = 99;
        int deptOfficerId = 1;

        int needId = 3;

        // User setup
        User affectedUser = new User();
        affectedUser.setUserId(affectedPersonId);
        affectedUser.setName("Akash");
        affectedUser.setMobileNumber("9876543210");
        affectedUser.setRole("AFFECTED_PERSON");

        User donor1User = new User();
        donor1User.setUserId(donor1Id);
        donor1User.setName("Suresh");
        donor1User.setMobileNumber("9876543211");
        donor1User.setRole("DONOR");

        User donor2User = new User();
        donor2User.setUserId(donor2Id);
        donor2User.setName("Ramesh");
        donor2User.setMobileNumber("9876543212");
        donor2User.setRole("DONOR");

        User unpledgedDonor = new User();
        unpledgedDonor.setUserId(unpledgedDonorId);
        unpledgedDonor.setName("Mahesh");
        unpledgedDonor.setMobileNumber("9876543213");
        unpledgedDonor.setRole("DONOR");

        User unrelatedCitizen = new User();
        unrelatedCitizen.setUserId(unrelatedCitizenId);
        unrelatedCitizen.setName("Stranger");
        unrelatedCitizen.setMobileNumber("9876543214");
        unrelatedCitizen.setRole("AFFECTED_PERSON");

        User deptOfficer = new User();
        deptOfficer.setUserId(deptOfficerId);
        deptOfficer.setName("Officer Roy");
        deptOfficer.setMobileNumber("9876543215");
        deptOfficer.setRole("DEPARTMENT");

        Map<Integer, User> userDb = new HashMap<>();
        userDb.put(affectedPersonId, affectedUser);
        userDb.put(donor1Id, donor1User);
        userDb.put(donor2Id, donor2User);
        userDb.put(unpledgedDonorId, unpledgedDonor);
        userDb.put(unrelatedCitizenId, unrelatedCitizen);
        userDb.put(deptOfficerId, deptOfficer);

        UserDAO mockUserDAO = new UserDAO(null) {
            @Override
            public User findById(int uId) {
                return userDb.get(uId);
            }
            @Override
            public User findByMobile(String mobile) {
                return userDb.values().stream().filter(u -> mobile.equals(u.getMobileNumber())).findFirst().orElse(null);
            }
        };

        // Need setup
        Need testNeed = new Need();
        testNeed.setNeedId(needId);
        testNeed.setCreatedBy(affectedPersonId);
        testNeed.setCategory("Medical & First Aid");
        testNeed.setStatus("VERIFIED");
        testNeed.setQuantityRequired(10);
        testNeed.setQuantityAllocated(6);
        testNeed.setQuantityRemaining(4);

        NeedDAO mockNeedDAO = new NeedDAO(null) {
            @Override
            public Need findById(int id) {
                return id == needId ? testNeed : null;
            }
            @Override
            public int updateQuantitiesAndStatus(int needId, int quantityPledged, int quantityAllocated, int quantityRemaining, String status) {
                testNeed.setQuantityAllocated(quantityAllocated);
                testNeed.setQuantityRemaining(quantityRemaining);
                testNeed.setStatus(status);
                return 1;
            }
        };

        // In-memory Need Participants Store
        List<NeedParticipant> participantDb = new ArrayList<>();
        NeedParticipantDAO mockParticipantDAO = new NeedParticipantDAO(null) {
            private int seq = 1;

            @Override
            public List<NeedParticipant> findByNeedAndUser(int nId, int uId) {
                return participantDb.stream()
                        .filter(p -> p.getNeedId() == nId && p.getUserId() == uId)
                        .toList();
            }

            @Override
            public List<NeedParticipant> findActiveByNeed(int nId) {
                return participantDb.stream()
                        .filter(p -> p.getNeedId() == nId && "ACTIVE".equalsIgnoreCase(p.getParticipationStatus()))
                        .toList();
            }

            @Override
            public void addOrActivateParticipant(int nId, int uId) {
                List<NeedParticipant> list = findByNeedAndUser(nId, uId);
                for (NeedParticipant existing : list) {
                    if ("ACTIVE".equalsIgnoreCase(existing.getParticipationStatus())) {
                        return;
                    }
                }
                NeedParticipant np = new NeedParticipant(seq++, nId, uId, new Timestamp(System.currentTimeMillis()), null, "ACTIVE");
                participantDb.add(np);
            }

            @Override
            public void withdrawParticipant(int nId, int uId) {
                List<NeedParticipant> list = findByNeedAndUser(nId, uId);
                for (NeedParticipant p : list) {
                    if ("ACTIVE".equalsIgnoreCase(p.getParticipationStatus())) {
                        p.setParticipationStatus("WITHDRAWN");
                        p.setLeftAt(new Timestamp(System.currentTimeMillis()));
                    }
                }
            }

            @Override
            public boolean isUserParticipantAt(int nId, int uId, Timestamp timestamp) {
                List<NeedParticipant> list = findByNeedAndUser(nId, uId);
                for (NeedParticipant p : list) {
                    if (p.isActiveDuring(timestamp)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean hasActiveParticipation(int nId, int uId) {
                List<NeedParticipant> list = findByNeedAndUser(nId, uId);
                return list.stream().anyMatch(p -> "ACTIVE".equalsIgnoreCase(p.getParticipationStatus()));
            }

            @Override
            public boolean hasAnyParticipation(int nId, int uId) {
                return !findByNeedAndUser(nId, uId).isEmpty();
            }
        };

        // In-memory Pledges Store
        List<Pledge> inMemoryPledges = new ArrayList<>();
        Pledge pledge1 = new Pledge();
        pledge1.setPledgeId(1);
        pledge1.setNeedId(needId);
        pledge1.setDonorId(donor1Id);
        pledge1.setQuantityOffered(6);
        pledge1.setQuantityAllocated(6);
        pledge1.setStatus("ALLOCATED");
        inMemoryPledges.add(pledge1);
        mockParticipantDAO.addOrActivateParticipant(needId, donor1Id);

        PledgeDAO mockPledgeDAO = new PledgeDAO(null) {
            @Override
            public List<Pledge> findByNeedId(int nId) {
                return inMemoryPledges.stream().filter(p -> p.getNeedId() == nId).toList();
            }
            @Override
            public List<Pledge> findByDonorId(int dId) {
                return inMemoryPledges.stream().filter(p -> p.getDonorId() == dId).toList();
            }
            @Override
            public Pledge findById(int pId) {
                return inMemoryPledges.stream().filter(p -> p.getPledgeId() == pId).findFirst().orElse(null);
            }
            @Override
            public int updateStatus(int pId, String status) {
                Pledge p = findById(pId);
                if (p != null) {
                    p.setStatus(status);
                    return 1;
                }
                return 0;
            }
            @Override
            public int save(Pledge p) {
                p.setPledgeId(inMemoryPledges.size() + 1);
                inMemoryPledges.add(p);
                return p.getPledgeId();
            }
        };

        // In-memory AllocationDAO
        List<Allocation> inMemoryAllocations = new ArrayList<>();
        AllocationDAO mockAllocationDAO = new AllocationDAO(null) {
            @Override
            public int save(Allocation a) {
                inMemoryAllocations.add(a);
                return 1;
            }
            @Override
            public List<Allocation> findByNeedId(int nId) {
                return inMemoryAllocations.stream().filter(a -> a.getNeedId() == nId).toList();
            }
        };

        // In-memory Message Store
        List<Message> inMemoryDb = new ArrayList<>();
        MessageDAO mockMessageDAO = new MessageDAO(null) {
            private int idSeq = 1;

            @Override
            public int save(Message message) {
                Message saved = new Message();
                saved.setMessageId(idSeq++);
                saved.setNeedId(message.getNeedId());
                saved.setSenderId(message.getSenderId());
                saved.setReceiverId(message.getReceiverId());
                saved.setMessage(message.getMessage());
                saved.setMessageType(message.getMessageType());
                saved.setSentAt(message.getSentAt() != null ? message.getSentAt() : new Timestamp(System.currentTimeMillis()));
                User sender = userDb.get(saved.getSenderId());
                saved.setSenderName(sender != null ? sender.getName() : "User");
                inMemoryDb.add(saved);
                return 1;
            }

            @Override
            public List<Message> findByNeedId(int nId) {
                return inMemoryDb.stream().filter(m -> m.getNeedId() == nId).toList();
            }
        };

        AlertDAO mockAlertDAO = new AlertDAO(null);

        CommunicationService commService = new CommunicationService(
                mockMessageDAO, mockAlertDAO, mockNeedDAO, mockPledgeDAO, mockUserDAO, mockParticipantDAO);
        CommunicationController commController = new CommunicationController(commService);

        PledgeAllocationService pledgeService = new PledgeAllocationService(
                mockPledgeDAO, mockAllocationDAO, mockNeedDAO, mockParticipantDAO);
        PledgeAllocationController pledgeController = new PledgeAllocationController(pledgeService);

        // Sessions
        HttpSession affectedSession = createMockSession(new HashMap<>(Map.of("loggedInUser", affectedUser)));
        HttpSession donor1Session = createMockSession(new HashMap<>(Map.of("loggedInUser", donor1User)));
        HttpSession donor2Session = createMockSession(new HashMap<>(Map.of("loggedInUser", donor2User)));
        HttpSession unpledgedSession = createMockSession(new HashMap<>(Map.of("loggedInUser", unpledgedDonor)));
        HttpSession unrelatedSession = createMockSession(new HashMap<>(Map.of("loggedInUser", unrelatedCitizen)));
        HttpSession deptSession = createMockSession(new HashMap<>(Map.of("loggedInUser", deptOfficer)));

        // =============================================================
        // Check 1: Direct message Akash -> Suresh (DIRECT, receiver 6)
        // =============================================================
        ResponseEntity<?> c1 = commController.postMessage(
                needId, donor1Id, "Hello Suresh, thank you for pledging.", "DIRECT", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.CREATED, c1.getStatusCode(), "Check 1: Status must be 201 Created");
        assertEquals(1, inMemoryDb.size(), "Check 1: 1 message in DB");
        Message msg1 = inMemoryDb.get(0);
        assertEquals(affectedPersonId, msg1.getSenderId(), "Check 1: Sender is Akash");
        assertEquals(donor1Id, (int) msg1.getReceiverId(), "Check 1: Receiver is Suresh");
        assertEquals("DIRECT", msg1.getMessageType(), "Check 1: message_type is DIRECT");
        System.out.println("[PASS] Check 1: Direct message Akash -> Suresh (DIRECT, receiver=6).");

        // =============================================================
        // Check 2: Direct reply Suresh -> Akash (DIRECT, receiver 4)
        // =============================================================
        ResponseEntity<?> c2 = commController.postMessage(
                needId, affectedPersonId, "You're welcome Akash! Happy to help.", "DIRECT", donor1Id, donor1Session);
        assertEquals(HttpStatus.CREATED, c2.getStatusCode(), "Check 2: Status must be 201 Created");
        assertEquals(2, inMemoryDb.size(), "Check 2: 2 messages in DB");
        Message msg2 = inMemoryDb.get(1);
        assertEquals(donor1Id, msg2.getSenderId(), "Check 2: Sender is Suresh");
        assertEquals(affectedPersonId, (int) msg2.getReceiverId(), "Check 2: Receiver is Akash");
        assertEquals("DIRECT", msg2.getMessageType(), "Check 2: message_type is DIRECT");
        System.out.println("[PASS] Check 2: Direct reply Suresh -> Akash (DIRECT, receiver=4).");

        // =============================================================
        // Check 3: Broadcast with 0 active donors (receiver null)
        // =============================================================
        int emptyNeedId = 999;
        Need emptyNeed = new Need();
        emptyNeed.setNeedId(emptyNeedId);
        emptyNeed.setCreatedBy(affectedPersonId);
        emptyNeed.setCategory("Clean Drinking Water");
        emptyNeed.setStatus("VERIFIED");
        NeedDAO mockNeedWithEmptyDAO = new NeedDAO(null) {
            @Override
            public Need findById(int id) {
                if (id == emptyNeedId) return emptyNeed;
                return id == needId ? testNeed : null;
            }
        };
        CommunicationService commServiceEmpty = new CommunicationService(
                mockMessageDAO, mockAlertDAO, mockNeedWithEmptyDAO, mockPledgeDAO, mockUserDAO, mockParticipantDAO);
        CommunicationController commControllerEmpty = new CommunicationController(commServiceEmpty);

        ResponseEntity<?> c3 = commControllerEmpty.postMessage(
                emptyNeedId, null, "Urgent: Need water bottles at shelter 4.", "BROADCAST", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.CREATED, c3.getStatusCode(), "Check 3: Broadcast with 0 donors must return 201 Created");
        Message msg3 = inMemoryDb.get(2);
        assertEquals(null, msg3.getReceiverId(), "Check 3: receiver_id must be null");
        assertEquals("BROADCAST", msg3.getMessageType(), "Check 3: message_type must be BROADCAST");
        System.out.println("[PASS] Check 3: Broadcast with 0 donors saved with receiver_id=null and message_type='BROADCAST'.");

        // =============================================================
        // Check 4: Broadcast with multiple donors (received by eligible participants)
        // =============================================================
        Pledge pledge2 = new Pledge();
        pledge2.setPledgeId(2);
        pledge2.setNeedId(needId);
        pledge2.setDonorId(donor2Id);
        pledge2.setQuantityOffered(4);
        pledge2.setQuantityAllocated(4);
        pledge2.setStatus("ALLOCATED");
        inMemoryPledges.add(pledge2);
        mockParticipantDAO.addOrActivateParticipant(needId, donor2Id);

        ResponseEntity<?> c4 = commController.postMessage(
                needId, null, "Attention all donors: medical supplies will be collected at 10 AM.", "BROADCAST", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.CREATED, c4.getStatusCode(), "Check 4: Broadcast to multiple donors returns 201");
        Message msg4 = inMemoryDb.get(3);
        assertEquals(null, msg4.getReceiverId(), "Check 4: receiver_id is null");
        assertEquals("BROADCAST", msg4.getMessageType(), "Check 4: message_type is BROADCAST");

        // Both Suresh (Donor 1) and Ramesh (Donor 2) must receive this broadcast
        List<Message> donor1List = commService.getMessagesForNeedAndUser(needId, donor1User);
        List<Message> donor2List = commService.getMessagesForNeedAndUser(needId, donor2User);
        assertTrue(donor1List.stream().anyMatch(m -> "BROADCAST".equals(m.getMessageType()) && m.getMessage().contains("Attention all donors")),
                "Donor 1 must receive the broadcast message");
        assertTrue(donor2List.stream().anyMatch(m -> "BROADCAST".equals(m.getMessageType()) && m.getMessage().contains("Attention all donors")),
                "Donor 2 must receive the broadcast message");
        System.out.println("[PASS] Check 4: Broadcast with multiple active donors received by both eligible participants.");

        // =============================================================
        // Check 5: Direct reply in multi-donor thread isolated from other donors
        // =============================================================
        ResponseEntity<?> c5a = commController.postMessage(
                needId, affectedPersonId, "Hi Akash, Ramesh here. Dropping off at Clinic B.", "DIRECT", donor2Id, donor2Session);
        assertEquals(HttpStatus.CREATED, c5a.getStatusCode(), "Check 5: Donor 2 direct message returns 201");

        ResponseEntity<?> c5b = commController.postMessage(
                needId, donor2Id, "Thanks Ramesh, Clinic B staff will receive it.", "DIRECT", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.CREATED, c5b.getStatusCode(), "Check 5: Direct reply to Donor 2 returns 201");

        // Donor 1 must NOT see Donor 2's direct messages or replies!
        List<Message> donor1Visible = commService.getMessagesForNeedAndUser(needId, donor1User);
        boolean donor1SeesDonor2 = donor1Visible.stream().anyMatch(m ->
                m.getSenderId() == donor2Id || (m.getReceiverId() != null && m.getReceiverId() == donor2Id));
        assertTrue(!donor1SeesDonor2, "Check 5: Donor 1 must NEVER see Donor 2's private messages or replies");

        // Donor 2 must NOT see Donor 1's direct messages from Check 1 and Check 2!
        List<Message> donor2Visible = commService.getMessagesForNeedAndUser(needId, donor2User);
        boolean donor2SeesDonor1 = donor2Visible.stream().anyMatch(m ->
                m.getSenderId() == donor1Id || (m.getReceiverId() != null && m.getReceiverId() == donor1Id));
        assertTrue(!donor2SeesDonor1, "Check 5: Donor 2 must NEVER see Donor 1's private messages or replies");

        System.out.println("[PASS] Check 5: Direct messages between Akash and Donor 2 are completely isolated from Donor 1.");

        // =============================================================
        // Check 6: Unpledged donor accessing verified need: cannot see conversation or broadcasts
        // =============================================================
        ResponseEntity<?> c6 = commController.getMessages(needId, unpledgedDonorId, unpledgedSession);
        assertEquals(HttpStatus.FORBIDDEN, c6.getStatusCode(),
                "Check 6: Unpledged donor accessing verified need must receive 403 Forbidden");

        ResponseEntity<?> c6Post = commController.postMessage(
                needId, null, "Trying to message without pledging", "DIRECT", unpledgedDonorId, unpledgedSession);
        assertEquals(HttpStatus.FORBIDDEN, c6Post.getStatusCode(),
                "Check 6: Unpledged donor posting to verified need must receive 403 Forbidden");
        System.out.println("[PASS] Check 6: Unpledged donor forbidden (403) from accessing conversation or broadcasts of VERIFIED need.");

        // =============================================================
        // Check 7: Donor withdraws pledge: retains historical messages in [joined_at, left_at], cannot see post-withdrawal broadcasts
        // =============================================================
        pledgeService.withdrawPledge(1, donor1Id);
        List<NeedParticipant> npDonor1List = mockParticipantDAO.findByNeedAndUser(needId, donor1Id);
        assertTrue(!npDonor1List.isEmpty(), "Donor 1 participant record exists");
        NeedParticipant npDonor1 = npDonor1List.get(0);
        assertEquals("WITHDRAWN", npDonor1.getParticipationStatus(), "Donor 1 participation status must be WITHDRAWN");
        assertTrue(npDonor1.getLeftAt() != null, "Donor 1 left_at must be populated");

        // Historical broadcast (Check 4) was sent BEFORE left_at, so Donor 1 still sees it
        List<Message> donor1AfterWithdraw = commService.getMessagesForNeedAndUser(needId, donor1User);
        assertTrue(donor1AfterWithdraw.stream().anyMatch(m -> m.getMessage().contains("Attention all donors")),
                "Check 7: Withdrawn donor retains access to historical broadcast sent while active");

        // Now, Akash sends a NEW broadcast AFTER Donor 1 has withdrawn
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        ResponseEntity<?> postWithdrawBroadcast = commController.postMessage(
                needId, null, "Post-withdrawal broadcast: extra kits arrived.", "BROADCAST", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.CREATED, postWithdrawBroadcast.getStatusCode(), "Post-withdrawal broadcast created");

        List<Message> donor1AfterNewBroadcast = commService.getMessagesForNeedAndUser(needId, donor1User);
        boolean donor1SeesNewBroadcast = donor1AfterNewBroadcast.stream().anyMatch(m -> m.getMessage().contains("Post-withdrawal broadcast"));
        assertTrue(!donor1SeesNewBroadcast, "Check 7: Withdrawn donor must NOT see broadcasts sent after withdrawal");

        // Active Donor 2 MUST see the new broadcast
        List<Message> donor2AfterNewBroadcast = commService.getMessagesForNeedAndUser(needId, donor2User);
        assertTrue(donor2AfterNewBroadcast.stream().anyMatch(m -> m.getMessage().contains("Post-withdrawal broadcast")),
                "Check 7: Active Donor 2 sees the post-withdrawal broadcast");
        System.out.println("[PASS] Check 7: Withdrawn donor retains historical broadcast in [joined_at, left_at], excluded from new broadcasts.");

        // =============================================================
        // Check 8: Donor rejoins: gets access to new broadcasts
        // =============================================================
        pledgeService.createPledgeAndAllocate(needId, donor1Id, 5);
        List<NeedParticipant> npDonor1RejoinedList = mockParticipantDAO.findByNeedAndUser(needId, donor1Id);
        assertTrue(npDonor1RejoinedList.stream().anyMatch(p -> "ACTIVE".equalsIgnoreCase(p.getParticipationStatus())),
                "Donor 1 status must have an ACTIVE record upon rejoining");

        // A new broadcast is posted
        ResponseEntity<?> postRejoinBroadcast = commController.postMessage(
                needId, null, "Broadcast after Donor 1 rejoins: thank you for renewed support.", "BROADCAST", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.CREATED, postRejoinBroadcast.getStatusCode(), "Broadcast created");

        List<Message> donor1AfterRejoin = commService.getMessagesForNeedAndUser(needId, donor1User);
        assertTrue(donor1AfterRejoin.stream().anyMatch(m -> m.getMessage().contains("Broadcast after Donor 1 rejoins")),
                "Check 8: Rejoined donor must receive new broadcasts");
        System.out.println("[PASS] Check 8: Donor rejoins and receives subsequent broadcast messages.");

        // =============================================================
        // Check 9: Department broadcast: visible to participants
        // =============================================================
        ResponseEntity<?> deptBroadcastResp = commController.postMessage(
                needId, null, "Department Alert: Emergency dispatch en route.", "BROADCAST", deptOfficerId, deptSession);
        assertEquals(HttpStatus.CREATED, deptBroadcastResp.getStatusCode(), "Check 9: Department broadcast returns 201");

        List<Message> affectedViewDept = commService.getMessagesForNeedAndUser(needId, affectedUser);
        List<Message> donor1ViewDept = commService.getMessagesForNeedAndUser(needId, donor1User);
        assertTrue(affectedViewDept.stream().anyMatch(m -> m.getMessage().contains("Department Alert")),
                "Check 9: Affected person sees department broadcast");
        assertTrue(donor1ViewDept.stream().anyMatch(m -> m.getMessage().contains("Department Alert")),
                "Check 9: Active donor sees department broadcast");
        System.out.println("[PASS] Check 9: Department broadcast visible to both affected person and active donors.");

        // =============================================================
        // Check 10: Deleted recipient: direct message retains message_type = 'DIRECT'
        // =============================================================
        Message directMsgWithDeletedReceiver = new Message();
        directMsgWithDeletedReceiver.setNeedId(needId);
        directMsgWithDeletedReceiver.setSenderId(donor2Id);
        directMsgWithDeletedReceiver.setReceiverId(null); // Foreign key set receiver_id to NULL
        directMsgWithDeletedReceiver.setMessage("Direct message whose receiver account was closed");
        directMsgWithDeletedReceiver.setMessageType("DIRECT"); // Remains DIRECT!
        mockMessageDAO.save(directMsgWithDeletedReceiver);

        // Donor 1 must NOT see this message, because message_type is DIRECT and Donor 1 is neither sender nor receiver
        List<Message> donor1WithDeleted = commService.getMessagesForNeedAndUser(needId, donor1User);
        boolean donor1SeesDeleted = donor1WithDeleted.stream().anyMatch(m -> m.getMessage().contains("whose receiver account was closed"));
        assertTrue(!donor1SeesDeleted, "Check 10: DIRECT message with receiver_id=NULL must NOT leak to other donors as a broadcast");
        System.out.println("[PASS] Check 10: Deleted recipient direct message retains message_type='DIRECT' and never leaks as a broadcast.");

        // =============================================================
        // Check 11: Session ID regeneration check (verifyOtp and registerUser)
        // =============================================================
        AtomicBoolean changeSessionIdCalled = new AtomicBoolean(false);
        Map<String, Object> authSessionAttrs = new HashMap<>();
        authSessionAttrs.put("pendingMobile", "9876543211");
        HttpSession authSession = createMockSession(authSessionAttrs);
        HttpServletRequest testReq = createMockRequest(authSession, changeSessionIdCalled);

        AuthService mockAuthService = new AuthService(mockUserDAO) {
            @Override
            public boolean verifyOtp(String mobile, String otp) {
                return "123456".equals(otp);
            }
            @Override
            public User findUserByMobile(String mobile) {
                return donor1User;
            }
        };
        AuthController authController = new AuthController(mockAuthService);
        authController.verifyOtp("123456", testReq, authSession);
        assertTrue(changeSessionIdCalled.get(), "Check 11: request.changeSessionId() MUST be called upon successful OTP verification");
        System.out.println("[PASS] Check 11: Session fixation defense verified (request.changeSessionId() called).");

        // =============================================================
        // Check 12: Stale user / X-Expected-User-Id mismatch returning 409 Conflict
        // =============================================================
        ResponseEntity<?> mismatchGet = commController.getMessages(needId, 99, affectedSession);
        assertEquals(HttpStatus.CONFLICT, mismatchGet.getStatusCode(),
                "Check 12: GET with X-Expected-User-Id mismatch must return 409 Conflict");

        ResponseEntity<?> mismatchPost = commController.postMessage(
                needId, donor1Id, "Testing mismatch", "DIRECT", 99, affectedSession);
        assertEquals(HttpStatus.CONFLICT, mismatchPost.getStatusCode(),
                "Check 12: POST with X-Expected-User-Id mismatch must return 409 Conflict");
        System.out.println("[PASS] Check 12: Mismatched X-Expected-User-Id returns 409 Conflict (session collision prevented).");

        // =============================================================
        // Check 13: Input validation, empty/whitespace/null, unauthenticated (401), invalid receiver (400)
        // =============================================================
        // 13a. Unauthenticated session
        HttpSession emptySession = createMockSession(new HashMap<>());
        ResponseEntity<?> noLogin = commController.getMessages(needId, null, emptySession);
        assertEquals(HttpStatus.UNAUTHORIZED, noLogin.getStatusCode(), "Check 13a: Unauthenticated GET returns 401");

        // 13b. Empty message
        ResponseEntity<?> emptyMsg = commController.postMessage(needId, donor1Id, "", "DIRECT", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.BAD_REQUEST, emptyMsg.getStatusCode(), "Check 13b: Empty message returns 400");

        // 13c. Whitespace message
        ResponseEntity<?> spaceMsg = commController.postMessage(needId, donor1Id, "   ", "DIRECT", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.BAD_REQUEST, spaceMsg.getStatusCode(), "Check 13c: Whitespace message returns 400");

        // 13d. Null message
        ResponseEntity<?> nullMsg = commController.postMessage(needId, donor1Id, null, "DIRECT", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.BAD_REQUEST, nullMsg.getStatusCode(), "Check 13d: Null message returns 400");

        // 13e. Donor attempting to message non-affected user
        ResponseEntity<?> donorToOtherDonor = commController.postMessage(needId, donor2Id, "Hey donor", "DIRECT", donor1Id, donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, donorToOtherDonor.getStatusCode(), "Check 13e: Donor messaging another donor returns 400");

        // 13f. User messaging self
        ResponseEntity<?> selfMsg = commController.postMessage(needId, donor1Id, "Self talk", "DIRECT", donor1Id, donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, selfMsg.getStatusCode(), "Check 13f: User messaging themselves returns 400");

        // 13g. Affected person attempting to DIRECT message unrelated stranger
        ResponseEntity<?> affectedToStrang = commController.postMessage(needId, unrelatedCitizenId, "Hello", "DIRECT", affectedPersonId, affectedSession);
        assertEquals(HttpStatus.BAD_REQUEST, affectedToStrang.getStatusCode(), "Check 13g: Affected person messaging unrelated user returns 400");

        System.out.println("[PASS] Check 13: Input validation, 401 unauthenticated, and 400 invalid recipient checks all passed.");

        System.out.println("===============================================================");
        System.out.println("=== ALL 13 VERIFICATION CHECKS COMPLETED SUCCESSFULLY (100%) ===");
        System.out.println("===============================================================");
    }
}
