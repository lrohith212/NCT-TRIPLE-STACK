package com.nodueplus.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.nodueplus.platform.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private static final String STAFF_SECRET_KEY = "NCT_HACK_2025"; 

    @GetMapping("/")
    public String showAuthPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; 
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, 
                               @RequestParam(required = false) String adminCode,
                               HttpSession session, 
                               Model model) {
        Firestore db = FirestoreClient.getFirestore();

        if ("STAFF".equals(user.getRole())) {
            if (!STAFF_SECRET_KEY.equals(adminCode)) {
                model.addAttribute("error", "Invalid Admin Secret Key!");
                return "register";
            }
            try {
                db.collection("staff_users").document(user.getId()).set(user);
                session.setAttribute("currentUser", user);
                session.setAttribute("userRole", "STAFF"); // Fix: Set explicit role
                return "redirect:/staff-dashboard"; 
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error: " + e.getMessage());
                return "register";
            }
        } else {
            user.setRole("STUDENT");
            try {
                db.collection("students_v2").document(user.getId()).set(user);
                session.setAttribute("currentUser", user);
                session.setAttribute("userRole", "STUDENT"); // Fix: Set explicit role
                return "redirect:/student-dashboard"; 
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error: " + e.getMessage());
                return "register";
            }
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String id, 
                            @RequestParam String password, 
                            @RequestParam String role,
                            HttpSession session, 
                            Model model) {
        
        Firestore db = FirestoreClient.getFirestore();
        String collectionName = "STAFF".equals(role) ? "staff_users" : "students_v2";

        try {
            DocumentSnapshot document = db.collection(collectionName).document(id).get().get();

            if (document.exists()) {
                User user = document.toObject(User.class);
                if (user != null && user.getPassword().equals(password)) {
                    user.setId(document.getId());
                    
                    // Fix: Explicitly store both user and role
                    session.setAttribute("currentUser", user);
                    session.setAttribute("userRole", role); 
                    
                    if ("STAFF".equals(role)) return "redirect:/staff-dashboard";
                    else return "redirect:/student-dashboard";
                } else {
                    model.addAttribute("error", "Incorrect Password!");
                    return "register";
                }
            } else {
                model.addAttribute("error", "User not found in " + collectionName);
                return "register";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/";
    }
}