package com.nodueplus.platform.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.nodueplus.platform.model.Event;
import com.nodueplus.platform.model.SemesterRequirement;
import com.nodueplus.platform.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class StaffController {

    @GetMapping("/staff-dashboard")
    public String showStaffDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        String role = (String) session.getAttribute("userRole");

        // FIX: Security check for Staff role
        if (currentUser == null || !"STAFF".equals(role)) {
            return "redirect:/";
        }
        
        model.addAttribute("user", currentUser);

        Firestore db = FirestoreClient.getFirestore();
        List<User> studentList = new ArrayList<>();
        
        try {
            List<QueryDocumentSnapshot> documents = db.collection("students_v2").get().get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                User u = document.toObject(User.class);
                u.setId(document.getId());
                if (u.getMyActivities() == null) u.setMyActivities(new ArrayList<>());
                studentList.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); }

        model.addAttribute("students", studentList);
        return "staff_dashboard"; 
    }

    @PostMapping("/set-requirements")
    public String setRequirements(@RequestParam String semester, @RequestParam String activities) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            String[] items = activities.split(",");
            List<String> activityList = new ArrayList<>();
            for (String item : items) activityList.add(item.trim());
            SemesterRequirement req = new SemesterRequirement(semester, activityList);
            // FIX: The document ID must match the Student's 'year' field
            db.collection("requirements").document(semester).set(req);
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/staff-dashboard";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam String studentId, @RequestParam String activityType, @RequestParam String status, @RequestParam(required = false) String reason) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            User student = db.collection("students_v2").document(studentId).get().get().toObject(User.class);
            if (student != null && student.getMyActivities() != null) {
                for (User.Activity act : student.getMyActivities()) {
                    if (act.getType().equals(activityType)) {
                        act.setStatus(status);
                        if ("REJECTED".equals(status)) act.setRejectionReason(reason);
                        else act.setRejectionReason(null);
                        break;
                    }
                }
                db.collection("students_v2").document(studentId).set(student);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/staff-dashboard";
    }

    @GetMapping("/download-report")
    public void downloadReport(jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=NoDue_Report.csv");
            Firestore db = FirestoreClient.getFirestore();
            List<QueryDocumentSnapshot> docs = db.collection("students_v2").get().get().getDocuments();
            StringBuilder csv = new StringBuilder();
            csv.append("Roll No,Name,Department,Year,Status\n");
            for (QueryDocumentSnapshot doc : docs) {
                User u = doc.toObject(User.class);
                String status = "Incomplete";
                if (u.getMyActivities() != null && !u.getMyActivities().isEmpty()) {
                    boolean allApproved = true;
                    for (User.Activity act : u.getMyActivities()) {
                        if (!"APPROVED".equals(act.getStatus())) { allApproved = false; break; }
                    }
                    if (allApproved) status = "Cleared";
                    else status = "Pending Review";
                }
                csv.append(doc.getId()).append(",").append(u.getName()).append(",").append(u.getDepartment()).append(",").append(u.getYear()).append(",").append(status).append("\n");
            }
            response.getWriter().write(csv.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @PostMapping("/post-event")
    public String postEvent(@RequestParam String title, @RequestParam String description, @RequestParam String eventDate, @RequestParam String deadline, @RequestParam String link, @RequestParam("poster") MultipartFile file) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            String base64Poster = null;
            if (!file.isEmpty()) base64Poster = Base64.getEncoder().encodeToString(file.getBytes());
            Event event = new Event(title, description, eventDate, deadline, link, base64Poster);
            db.collection("events").add(event);
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/staff-dashboard";
    }
}