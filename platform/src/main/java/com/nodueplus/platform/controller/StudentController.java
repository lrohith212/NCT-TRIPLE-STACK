package com.nodueplus.platform.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.nodueplus.platform.model.Event;
import com.nodueplus.platform.model.SemesterRequirement;
import com.nodueplus.platform.model.User;
import com.nodueplus.platform.model.User.Activity;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    @GetMapping("/student-dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        String role = (String) session.getAttribute("userRole");

        // FIX: Verify that the logged-in user is actually a student
        if (user == null || !"STUDENT".equals(role)) return "redirect:/";

        Firestore db = FirestoreClient.getFirestore();
        try {
            // Re-fetch the student record to ensure name and year are fresh
            DocumentSnapshot userDoc = db.collection("students_v2").document(user.getId()).get().get();
            if (userDoc.exists()) {
                User freshUser = userDoc.toObject(User.class);
                freshUser.setId(userDoc.getId());
                if (freshUser.getMyActivities() == null) freshUser.setMyActivities(new ArrayList<>());

                model.addAttribute("user", freshUser);

                // FIX: Assign requirements by matching the Student's Year to the Doc ID
                DocumentSnapshot reqDoc = db.collection("requirements")
                                            .document(freshUser.getYear()).get().get();
                
                if (reqDoc.exists()) {
                    SemesterRequirement req = reqDoc.toObject(SemesterRequirement.class);
                    model.addAttribute("requirements", req.getRequiredActivities());
                } else {
                    model.addAttribute("requirements", new ArrayList<String>());
                }

                // --- EVENTS LOGIC ---
                List<Event> eventList = new ArrayList<>();
                List<QueryDocumentSnapshot> eventDocs = db.collection("events").get().get().getDocuments();
                String today = LocalDate.now().toString();
                for (QueryDocumentSnapshot doc : eventDocs) {
                    Event evt = doc.toObject(Event.class);
                    if (evt.getDeadline() != null && evt.getDeadline().compareTo(today) >= 0) eventList.add(evt);
                }
                model.addAttribute("events", eventList);

                // --- LEADERBOARD LOGIC ---
                List<User> classmates = new ArrayList<>();
                List<QueryDocumentSnapshot> allStudents = db.collection("students_v2").get().get().getDocuments();
                for (QueryDocumentSnapshot doc : allStudents) {
                    User u = doc.toObject(User.class);
                    if (u.getDepartment().equals(freshUser.getDepartment()) && u.getYear().equals(freshUser.getYear())) classmates.add(u);
                }
                Collections.sort(classmates, (u1, u2) -> Integer.compare(getApprovedCount(u2), getApprovedCount(u1)));
                model.addAttribute("leaderboard", classmates.size() > 5 ? classmates.subList(0, 5) : classmates);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "student_dashboard"; 
    }

    private int getApprovedCount(User u) {
        if (u.getMyActivities() == null) return 0;
        int count = 0;
        for (Activity a : u.getMyActivities()) {
            if ("APPROVED".equals(a.getStatus())) count++;
        }
        return count;
    }

    @PostMapping("/upload-activity")
    public String uploadActivity(@RequestParam String activityType, @RequestParam String eventName,
                                 @RequestParam String place, @RequestParam String date,
                                 @RequestParam String college, @RequestParam("certificate") MultipartFile file,
                                 HttpSession session) {
        User sessionUser = (User) session.getAttribute("currentUser");
        if (sessionUser == null) return "redirect:/";

        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot doc = db.collection("students_v2").document(sessionUser.getId()).get().get();
            User user = doc.toObject(User.class);
            user.setId(sessionUser.getId());
            if (user.getMyActivities() == null) user.setMyActivities(new ArrayList<>());
            user.getMyActivities().removeIf(act -> act.getType().equals(activityType));

            Activity newActivity = new Activity();
            newActivity.setType(activityType);
            newActivity.setEventName(eventName);
            newActivity.setPlace(place);
            newActivity.setDate(date);
            newActivity.setCollege(college);
            newActivity.setStatus("PENDING");
            if (!file.isEmpty()) newActivity.setCertificateImage(Base64.getEncoder().encodeToString(file.getBytes()));
            
            user.getMyActivities().add(newActivity);
            db.collection("students_v2").document(user.getId()).set(user);
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/student-dashboard";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/";
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name, @RequestParam String department, @RequestParam String password, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/";
        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot doc = db.collection("students_v2").document(user.getId()).get().get();
            User dbUser = doc.toObject(User.class);
            if (dbUser != null) {
                dbUser.setName(name);
                dbUser.setDepartment(department);
                dbUser.setPassword(password);
                db.collection("students_v2").document(user.getId()).set(dbUser);
                session.setAttribute("currentUser", dbUser);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/student-dashboard";
    }
}