package com.relief;

import com.relief.controller.CommunicationController;
import com.relief.dao.AlertDAO;
import com.relief.dao.MessageDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.PledgeDAO;
import com.relief.dao.UserDAO;
import com.relief.model.Message;
import com.relief.model.Need;
import com.relief.model.Pledge;
import com.relief.model.User;
import com.relief.service.CommunicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        System.out.println("=== Running Messaging System Verification Suite ===");

        // Setup Test Data
        int affectedPersonId = 20;
        int donor1Id = 50;
        int donor2Id = 60;
        int unrelatedCitizenId = 99;
        int needId = 10;

        // Users
        User donor1User = new User();
        donor1User.setUserId(donor1Id);
        donor1User.setName("Donor Rahul");
        donor1User.setRole("DONOR");

        User donor2User = new User();
        donor2User.setUserId(donor2Id);
        donor2User.setName("Donor Amit");
        donor2User.setRole("DONOR");

        User affectedUser = new User();
        affectedUser.setUserId(affectedPersonId);
        affectedUser.setName("Citizen Priya");
        affectedUser.setRole("AFFECTED_PERSON");

        User unrelatedCitizen = new User();
        unrelatedCitizen.setUserId(unrelatedCitizenId);
        unrelatedCitizen.setName("Stranger");
        unrelatedCitizen.setRole("AFFECTED_PERSON");

        User deptOfficer = new User();
        deptOfficer.setUserId(1);
        deptOfficer.setName("Officer Roy");
        deptOfficer.setRole("DEPARTMENT");

        Map<Integer, User> userDb = new HashMap<>();
        userDb.put(donor1Id, donor1User);
        userDb.put(donor2Id, donor2User);
        userDb.put(affectedPersonId, affectedUser);
        userDb.put(unrelatedCitizenId, unrelatedCitizen);
        userDb.put(deptOfficer.getUserId(), deptOfficer);

        UserDAO mockUserDAO = new UserDAO(null) {
            @Override
            public User findById(int uId) {
                return userDb.get(uId);
            }
        };

        Need testNeed = new Need();
        testNeed.setNeedId(needId);
        testNeed.setCreatedBy(affectedPersonId);
        testNeed.setCategory("Food & Rations");
        testNeed.setStatus("VERIFIED");

        // In-memory message store to simulate database persistence
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
                saved.setContent(message.getContent());
                saved.setSentAt(new Timestamp(System.currentTimeMillis()));
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

        NeedDAO mockNeedDAO = new NeedDAO(null) {
            @Override
            public Need findById(int id) {
                return id == needId ? testNeed : null;
            }
        };

        List<Pledge> inMemoryPledges = new ArrayList<>();
        Pledge pledge1 = new Pledge();
        pledge1.setPledgeId(1);
        pledge1.setNeedId(needId);
        pledge1.setDonorId(donor1Id);
        inMemoryPledges.add(pledge1);

        Pledge pledge2 = new Pledge();
        pledge2.setPledgeId(2);
        pledge2.setNeedId(needId);
        pledge2.setDonorId(donor2Id);
        inMemoryPledges.add(pledge2);

        PledgeDAO mockPledgeDAO = new PledgeDAO(null) {
            @Override
            public List<Pledge> findByNeedId(int nId) {
                return nId == needId ? inMemoryPledges : List.of();
            }
        };

        AlertDAO mockAlertDAO = new AlertDAO(null);

        CommunicationService service = new CommunicationService(
                mockMessageDAO, mockAlertDAO, mockNeedDAO, mockPledgeDAO, mockUserDAO);

        CommunicationController controller = new CommunicationController(service);

        // Sessions
        HttpSession donor1Session = createMockSession(Map.of("loggedInUser", donor1User));
        HttpSession donor2Session = createMockSession(Map.of("loggedInUser", donor2User));
        HttpSession affectedSession = createMockSession(Map.of("loggedInUser", affectedUser));
        HttpSession unrelatedSession = createMockSession(Map.of("loggedInUser", unrelatedCitizen));
        HttpSession deptSession = createMockSession(Map.of("loggedInUser", deptOfficer));

        // -------------------------------------------------------------
        // Check 1: Donor messaging the correct affected person
        // -------------------------------------------------------------
        // Case 1a: Donor sends with receiverId = null -> backend auto-resolves to affectedPersonId
        ResponseEntity<?> sendResp1 = controller.postMessage(
                needId, null, "Hello Priya, I have pledged 50 ration kits.", donor1Session);
        assertEquals(HttpStatus.CREATED, sendResp1.getStatusCode(), "Donor message post should return 201 Created");
        assertEquals(1, inMemoryDb.size(), "Database must have 1 message persisted");

        Message m1 = inMemoryDb.get(0);
        assertEquals(donor1Id, m1.getSenderId(), "Sender must be Donor 1");
        assertEquals(affectedPersonId, (int) m1.getReceiverId(), "Receiver must auto-resolve to the Affected Person");

        // Case 1b: Donor sends with explicit receiverId = affectedPersonId -> allowed
        ResponseEntity<?> sendResp1b = controller.postMessage(
                needId, affectedPersonId, "Can drop them off at Camp 3.", donor1Session);
        assertEquals(HttpStatus.CREATED, sendResp1b.getStatusCode(), "Donor message with explicit affectedPersonId should return 201");
        assertEquals(2, inMemoryDb.size(), "Database must have 2 messages persisted");
        System.out.println("[PASS] Check 1: Donor messaging the correct affected person (auto-resolved & explicit).");

        // -------------------------------------------------------------
        // Check 2: Affected person replying to the correct donor
        // -------------------------------------------------------------
        // Affected person replies directly to Donor 1
        ResponseEntity<?> replyResp1 = controller.postMessage(
                needId, donor1Id, "Thank you Rahul! Camp 3 is open until 6 PM.", affectedSession);
        assertEquals(HttpStatus.CREATED, replyResp1.getStatusCode(), "Affected person reply should return 201 Created");
        assertEquals(3, inMemoryDb.size(), "Database must have 3 messages persisted");

        Message mReply1 = inMemoryDb.get(2);
        assertEquals(affectedPersonId, mReply1.getSenderId(), "Sender must be Affected Person");
        assertEquals(donor1Id, (int) mReply1.getReceiverId(), "Receiver must be Donor 1");
        System.out.println("[PASS] Check 2: Affected Person replying directly to the correct donor.");

        // -------------------------------------------------------------
        // Check 3: Multiple donors participating in the same need & message isolation
        // -------------------------------------------------------------
        // Donor 2 sends a message regarding their pledge
        ResponseEntity<?> sendResp2 = controller.postMessage(
                needId, null, "Hi Priya, Amit here. I pledged 30 medical kits.", donor2Session);
        assertEquals(HttpStatus.CREATED, sendResp2.getStatusCode(), "Donor 2 message should return 201 Created");
        assertEquals(4, inMemoryDb.size(), "Database must have 4 messages persisted");

        Message mDonor2 = inMemoryDb.get(3);
        assertEquals(donor2Id, mDonor2.getSenderId(), "Sender must be Donor 2");
        assertEquals(affectedPersonId, (int) mDonor2.getReceiverId(), "Receiver must be Affected Person");

        // Affected Person replies directly to Donor 2
        ResponseEntity<?> replyResp2 = controller.postMessage(
                needId, donor2Id, "Thanks Amit! Medical kits are urgently needed at Clinic B.", affectedSession);
        assertEquals(HttpStatus.CREATED, replyResp2.getStatusCode(), "Reply to Donor 2 should return 201 Created");
        assertEquals(5, inMemoryDb.size(), "Database must have 5 messages persisted");

        // Affected Person sends a broadcast message (receiverId = null in multi-donor thread)
        ResponseEntity<?> broadcastResp = controller.postMessage(
                needId, null, "Update for all donors: volunteer trucks will arrive at 4 PM.", affectedSession);
        assertEquals(HttpStatus.CREATED, broadcastResp.getStatusCode(), "Broadcast message should return 201 Created");
        assertEquals(6, inMemoryDb.size(), "Database must have 6 messages persisted");
        Message mBroadcast = inMemoryDb.get(5);
        assertEquals(null, mBroadcast.getReceiverId(), "Multi-donor unaddressed reply must have null receiver (broadcast)");

        // Verify Message Isolation:
        // Donor 1 should only see: Donor 1's messages (2), reply to Donor 1 (1), broadcast (1) = 4 messages total.
        // Donor 1 must NEVER see Donor 2's private messages or replies!
        ResponseEntity<?> donor1ViewResp = controller.getMessages(needId, donor1Session);
        @SuppressWarnings("unchecked")
        List<Message> donor1Msgs = (List<Message>) ((Map<String, Object>) donor1ViewResp.getBody()).get("messages");
        assertEquals(4, donor1Msgs.size(), "Donor 1 should only see 4 relevant messages (no Donor 2 private messages)");
        boolean donor1SeesDonor2 = donor1Msgs.stream().anyMatch(m -> m.getSenderId() == donor2Id || (m.getReceiverId() != null && m.getReceiverId() == donor2Id));
        assertTrue(!donor1SeesDonor2, "Donor 1 must NOT see Donor 2's private conversation");

        // Donor 2 should only see: Donor 2's message (1), reply to Donor 2 (1), broadcast (1) = 3 messages total.
        ResponseEntity<?> donor2ViewResp = controller.getMessages(needId, donor2Session);
        @SuppressWarnings("unchecked")
        List<Message> donor2Msgs = (List<Message>) ((Map<String, Object>) donor2ViewResp.getBody()).get("messages");
        assertEquals(3, donor2Msgs.size(), "Donor 2 should only see 3 relevant messages (no Donor 1 private messages)");
        boolean donor2SeesDonor1 = donor2Msgs.stream().anyMatch(m -> m.getSenderId() == donor1Id || (m.getReceiverId() != null && m.getReceiverId() == donor1Id));
        assertTrue(!donor2SeesDonor1, "Donor 2 must NOT see Donor 1's private conversation");

        // Affected Person sees all 6 messages
        ResponseEntity<?> affectedViewResp = controller.getMessages(needId, affectedSession);
        @SuppressWarnings("unchecked")
        List<Message> affectedMsgs = (List<Message>) ((Map<String, Object>) affectedViewResp.getBody()).get("messages");
        assertEquals(6, affectedMsgs.size(), "Affected Person should see all 6 thread messages");

        // Department Officer sees all 6 messages (oversight)
        ResponseEntity<?> deptViewResp = controller.getMessages(needId, deptSession);
        @SuppressWarnings("unchecked")
        List<Message> deptMsgs = (List<Message>) ((Map<String, Object>) deptViewResp.getBody()).get("messages");
        assertEquals(6, deptMsgs.size(), "Department Officer should have oversight access to all 6 messages");

        System.out.println("[PASS] Check 3: Multiple donors participating; message isolation and multi-donor broadcast verified.");

        // -------------------------------------------------------------
        // Check 4: Attempts to send messages to unrelated users
        // -------------------------------------------------------------
        // 4a. Donor attempts to message another donor instead of the affected person
        ResponseEntity<?> donorToDonor = controller.postMessage(
                needId, donor2Id, "Hey other donor, want to team up?", donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, donorToDonor.getStatusCode(),
                "Donor attempting to message non-affected person must return 400 Bad Request");

        // 4b. Donor attempts to message an unrelated user
        ResponseEntity<?> donorToStranger = controller.postMessage(
                needId, unrelatedCitizenId, "Hello stranger", donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, donorToStranger.getStatusCode(),
                "Donor attempting to message unrelated user must return 400 Bad Request");

        // 4c. Affected Person attempts to message an unrelated user (stranger who never pledged)
        ResponseEntity<?> affectedToStranger = controller.postMessage(
                needId, unrelatedCitizenId, "Can you help?", affectedSession);
        assertEquals(HttpStatus.BAD_REQUEST, affectedToStranger.getStatusCode(),
                "Affected person messaging unrelated user must return 400 Bad Request");

        // 4d. Non-existent recipient user ID
        ResponseEntity<?> nonExistentRec = controller.postMessage(
                needId, 99999, "Anyone there?", affectedSession);
        assertEquals(HttpStatus.BAD_REQUEST, nonExistentRec.getStatusCode(),
                "Messaging non-existent user must return 400 Bad Request");

        // 4e. User messaging themselves
        ResponseEntity<?> selfMessage = controller.postMessage(
                needId, donor1Id, "Talking to myself", donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, selfMessage.getStatusCode(),
                "Sending message to oneself must return 400 Bad Request");

        System.out.println("[PASS] Check 4: Attempts to send messages to unrelated or invalid users rejected with 400 Bad Request.");

        // -------------------------------------------------------------
        // Check 5: Unauthorized users attempting to retrieve or post conversations
        // -------------------------------------------------------------
        // 5a. Unrelated citizen (affected person of a different need) attempting to retrieve conversation
        ResponseEntity<?> unauthGet = controller.getMessages(needId, unrelatedSession);
        assertEquals(HttpStatus.FORBIDDEN, unauthGet.getStatusCode(),
                "Unauthorized user attempting to retrieve messages must return 403 Forbidden");

        // 5b. Unrelated citizen attempting to post to this conversation
        ResponseEntity<?> unauthPost = controller.postMessage(
                needId, null, "Intruding into chat", unrelatedSession);
        assertEquals(HttpStatus.FORBIDDEN, unauthPost.getStatusCode(),
                "Unauthorized user attempting to post message must return 403 Forbidden");

        // 5c. Direct service call defense-in-depth: getMessagesForNeedAndUser with unauthorized user returns empty list
        List<Message> unauthServiceFetch = service.getMessagesForNeedAndUser(needId, unrelatedCitizen);
        assertEquals(0, unauthServiceFetch.size(),
                "Service layer must return empty list for unauthorized user retrieval");

        System.out.println("[PASS] Check 5: Unauthorized users attempting to retrieve or post conversations rejected with 403 Forbidden.");

        // -------------------------------------------------------------
        // Check 6: Unauthenticated sessions and non-existent need handling
        // -------------------------------------------------------------
        HttpSession emptySession = createMockSession(new HashMap<>());
        ResponseEntity<?> noLoginGet = controller.getMessages(needId, emptySession);
        assertEquals(HttpStatus.UNAUTHORIZED, noLoginGet.getStatusCode(),
                "Unauthenticated GET must return 401 Unauthorized");

        ResponseEntity<?> noLoginPost = controller.postMessage(needId, null, "Hello", emptySession);
        assertEquals(HttpStatus.UNAUTHORIZED, noLoginPost.getStatusCode(),
                "Unauthenticated POST must return 401 Unauthorized");

        ResponseEntity<?> invalidNeed = controller.getMessages(88888, donor1Session);
        assertEquals(HttpStatus.FORBIDDEN, invalidNeed.getStatusCode(),
                "Non-existent need must return 403 Forbidden");

        System.out.println("[PASS] Check 6: Unauthenticated sessions return 401; non-existent needs return 403.");

        // -------------------------------------------------------------
        // Check 7: Empty, whitespace-only, and null messages rejected
        // -------------------------------------------------------------
        ResponseEntity<?> emptyMsg = controller.postMessage(needId, null, "", donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, emptyMsg.getStatusCode(), "Empty message returns 400");

        ResponseEntity<?> spaceMsg = controller.postMessage(needId, null, "   ", donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, spaceMsg.getStatusCode(), "Whitespace message returns 400");

        ResponseEntity<?> nullMsg = controller.postMessage(needId, null, null, donor1Session);
        assertEquals(HttpStatus.BAD_REQUEST, nullMsg.getStatusCode(), "Null message returns 400");

        System.out.println("[PASS] Check 7: Empty, whitespace-only, and null messages rejected with 400 Bad Request.");

        System.out.println("=== All 7 Messaging System Automated Verification Checks Passed Successfully (100%) ===");
    }
}
