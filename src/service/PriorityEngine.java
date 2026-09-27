package service;

import dao.ComplaintDAO;
import java.util.ArrayList;
import java.util.List;
import model.Complaint;
import model.Enums.ComplaintCategory;
import model.Enums.PriorityLevel;

/**
 * Intelligent rule-based Complaint Priority Engine.
 * Decoupled from UI and DB to allow future ML model or API plug-in.
 */
public class PriorityEngine {

    public interface IPriorityEngine {
        PriorityResult calculatePriority(Complaint complaint);
    }

    public static class PriorityResult {
        private final int score;
        private final PriorityLevel level;
        private final String reason;
        private final List<String> appliedRules;

        public PriorityResult(int score, PriorityLevel level, String reason, List<String> appliedRules) {
            this.score = score;
            this.level = level;
            this.reason = reason;
            this.appliedRules = appliedRules;
        }

        public int getScore() { return score; }
        public PriorityLevel getLevel() { return level; }
        public String getReason() { return reason; }
        public List<String> getAppliedRules() { return appliedRules; }

        @Override
        public String toString() {
            return level + " (Score: " + score + ") - " + reason;
        }
    }

    private final ComplaintDAO complaintDAO;

    public PriorityEngine() {
        this.complaintDAO = new ComplaintDAO();
    }

    public PriorityEngine(ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    /**
     * Calculates the 0-100 priority score and reasoning breakdown.
     */
    public PriorityResult calculatePriority(Complaint c) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        List<String> rules = new ArrayList<>();

        // 1. Base Category Severity
        ComplaintCategory cat = c.getCategoryEnum();
        int baseWeight = cat.getBaseWeight();
        score += baseWeight;
        reasons.add(cat.getDisplayName() + " base severity (+" + baseWeight + ")");
        rules.add("CATEGORY_BASE:" + baseWeight);

        // 2. Public Health Hazard
        if (c.isHealthRisk() || cat == ComplaintCategory.DEAD_ANIMAL_WASTE) {
            score += 30;
            reasons.add("Public health risk hazard (+30)");
            rules.add("HEALTH_RISK:+30");
        }

        // 3. Garbage Overflow Condition
        if (c.isGarbageOverflow() || cat == ComplaintCategory.GARBAGE_OVERFLOW) {
            score += 25;
            reasons.add("Active garbage overflow (+25)");
            rules.add("GARBAGE_OVERFLOW:+25");
        }

        // 4. Illegal Dumping Violation
        if (cat == ComplaintCategory.ILLEGAL_DUMPING) {
            score += 20;
            reasons.add("Illegal dumping violation (+20)");
            rules.add("ILLEGAL_DUMPING:+20");
        }

        // 5. Sensitive / High-Density Public Area
        if (c.isSensitiveArea()) {
            score += 15;
            reasons.add("Sensitive public zone / landmark (+15)");
            rules.add("SENSITIVE_AREA:+15");
        }

        // 6. Cluster Frequency (Multiple complaints in same ward within last 48 hours)
        if (c.getWardId() > 0 && complaintDAO != null) {
            int recentCount = complaintDAO.countRecentInWard(c.getWardId(), 48);
            if (recentCount >= 5) {
                score += 20;
                reasons.add("High ward cluster density (" + recentCount + " recent complaints, +20)");
                rules.add("WARD_CLUSTER_HIGH:+20");
            } else if (recentCount >= 2) {
                score += 10;
                reasons.add("Ward complaint recurrence (" + recentCount + " recent complaints, +10)");
                rules.add("WARD_CLUSTER_MEDIUM:+10");
            }
        }

        // 7. Pending Age Recalculation (if complaint was already in DB > 24 hours)
        if (c.getCreatedAt() != null) {
            long diffMillis = System.currentTimeMillis() - c.getCreatedAt().getTime();
            long diffHours = diffMillis / (1000 * 60 * 60);
            if (diffHours >= 48) {
                score += 25;
                reasons.add("Aging backlog (> 48h pending, +25)");
                rules.add("AGING_48H:+25");
            } else if (diffHours >= 24) {
                score += 15;
                reasons.add("Aging backlog (> 24h pending, +15)");
                rules.add("AGING_24H:+15");
            }
        }

        // Clamp score between 0 and 100
        if (score > 100) score = 100;
        if (score < 0) score = 0;

        // Map score to Priority Level
        PriorityLevel level;
        if (score >= 90) {
            level = PriorityLevel.CRITICAL;
        } else if (score >= 70) {
            level = PriorityLevel.HIGH;
        } else if (score >= 40) {
            level = PriorityLevel.MEDIUM;
        } else {
            level = PriorityLevel.LOW;
        }

        String combinedReason = String.join(" • ", reasons);
        return new PriorityResult(score, level, combinedReason, rules);
    }
}
