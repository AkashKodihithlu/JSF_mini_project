package com.relief.controller;

import com.relief.model.User;
import com.relief.service.CommunicationService;
import com.relief.service.NeedService;
import com.relief.service.PledgeAllocationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final NeedService needService;
    private final PledgeAllocationService pledgeAllocationService;
    private final CommunicationService communicationService;

    public DashboardController(NeedService needService,
            PledgeAllocationService pledgeAllocationService,
            CommunicationService communicationService) {
        this.needService = needService;
        this.pledgeAllocationService = pledgeAllocationService;
        this.communicationService = communicationService;
    }

    @GetMapping("/affected/dashboard")
    public String affectedDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (!"AFFECTED_PERSON".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("disasters", needService.getActiveDisasters());
        model.addAttribute("myNeeds", needService.getNeedsByUser(user.getUserId()));
        model.addAttribute("alerts", communicationService.getRelevantAlertsForUser(user));
        return "dashboard/affected-dashboard";
    }

    @GetMapping("/donor/dashboard")
    public String donorDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (!"DONOR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("matchedNeeds", pledgeAllocationService.getMatchedNeedsForDonor(user));
        model.addAttribute("myPledges", pledgeAllocationService.getPledgesByDonor(user.getUserId()));
        model.addAttribute("alerts", communicationService.getRelevantAlertsForUser(user));
        return "dashboard/donor-dashboard";
    }

    @GetMapping("/department/dashboard")
    public String departmentDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (!"DEPARTMENT".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("disasters", needService.getAllDisasters());
        model.addAttribute("pendingNeeds", needService.getPendingNeeds());
        model.addAttribute("allNeeds", needService.getAllNeeds());
        model.addAttribute("surplusPledges", pledgeAllocationService.getSurplusPledges());
        model.addAttribute("allocations", pledgeAllocationService.getAllAllocations());
        model.addAttribute("alerts", communicationService.getAllAlerts());
        return "dashboard/department-dashboard";
    }
}