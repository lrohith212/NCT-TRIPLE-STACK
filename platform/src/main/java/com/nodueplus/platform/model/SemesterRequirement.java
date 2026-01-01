package com.nodueplus.platform.model;
import lombok.Data;
import java.util.List;

@Data
public class SemesterRequirement {
    private String semester; // e.g., "4"
    private List<String> requiredActivities; // e.g., ["Workshop", "Hackathon", "NPTEL"]
    
    public SemesterRequirement() {}
    public SemesterRequirement(String semester, List<String> requiredActivities) {
        this.semester = semester;
        this.requiredActivities = requiredActivities;
    }
}