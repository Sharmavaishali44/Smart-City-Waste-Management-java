package model;

import java.awt.Color;

public class Enums {

    public enum UserRole {
        CITIZEN("Citizen"),
        ADMIN("Municipal Admin / Staff"),
        CREW("Collection Crew");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ComplaintStatus {
        NEW("New", new Color(59, 130, 246), new Color(239, 246, 255)),                        // Blue
        PENDING_ASSIGNMENT("Pending Assignment", new Color(234, 88, 12), new Color(255, 247, 237)), // Orange
        ASSIGNED("Assigned", new Color(168, 85, 247), new Color(250, 245, 255)),               // Purple
        IN_PROGRESS("In Progress", new Color(245, 158, 11), new Color(254, 243, 199)),         // Amber
        RESOLVED("Resolved", new Color(16, 185, 129), new Color(236, 253, 245)),                // Green
        REJECTED("Rejected", new Color(239, 68, 68), new Color(254, 242, 242));                // Red

        private final String displayName;
        private final Color foreground;
        private final Color background;

        ComplaintStatus(String displayName, Color foreground, Color background) {
            this.displayName = displayName;
            this.foreground = foreground;
            this.background = background;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getForeground() {
            return foreground;
        }

        public Color getBackground() {
            return background;
        }

        public static ComplaintStatus fromString(String str) {
            if (str == null) return NEW;
            String s = str.trim().toUpperCase().replace(" ", "_");
            try {
                return ComplaintStatus.valueOf(s);
            } catch (IllegalArgumentException e) {
                return NEW;
            }
        }
    }

    public enum PriorityLevel {
        CRITICAL("CRITICAL", new Color(239, 68, 68), new Color(254, 226, 226)),   // Red
        HIGH("HIGH", new Color(249, 115, 22), new Color(255, 237, 213)),          // Orange
        MEDIUM("MEDIUM", new Color(245, 158, 11), new Color(254, 243, 199)),       // Amber
        LOW("LOW", new Color(16, 185, 129), new Color(209, 250, 229));            // Green

        private final String displayName;
        private final Color foreground;
        private final Color background;

        PriorityLevel(String displayName, Color foreground, Color background) {
            this.displayName = displayName;
            this.foreground = foreground;
            this.background = background;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getForeground() {
            return foreground;
        }

        public Color getBackground() {
            return background;
        }

        public static PriorityLevel fromString(String str) {
            if (str == null) return MEDIUM;
            try {
                return PriorityLevel.valueOf(str.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return MEDIUM;
            }
        }
    }

    public enum ComplaintCategory {
        GARBAGE_NOT_COLLECTED("Garbage not collected", 15),
        GARBAGE_OVERFLOW("Garbage overflow", 25),
        ILLEGAL_DUMPING("Illegal dumping", 20),
        DEAD_ANIMAL_WASTE("Dead animal waste", 30),
        CONSTRUCTION_WASTE("Construction waste", 20),
        DRAINAGE_WASTE_ISSUE("Drainage/waste issue", 25),
        STREET_CLEANLINESS("Street cleanliness issue", 15),
        PLASTIC_WASTE("Plastic waste", 15),
        OTHER("Other", 10);

        private final String displayName;
        private final int baseWeight;

        ComplaintCategory(String displayName, int baseWeight) {
            this.displayName = displayName;
            this.baseWeight = baseWeight;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getBaseWeight() {
            return baseWeight;
        }

        public static ComplaintCategory fromDisplayName(String name) {
            if (name == null) return OTHER;
            for (ComplaintCategory cat : values()) {
                if (cat.displayName.equalsIgnoreCase(name) || cat.name().equalsIgnoreCase(name)) {
                    return cat;
                }
            }
            return OTHER;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum CrewAvailability {
        AVAILABLE("Available"),
        ON_DUTY("On Duty"),
        OFF_DUTY("Off Duty"),
        INACTIVE("Inactive");

        private final String displayName;

        CrewAvailability(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CrewAvailability fromString(String str) {
            if (str == null) return AVAILABLE;
            String s = str.trim().toUpperCase().replace(" ", "_");
            try {
                return CrewAvailability.valueOf(s);
            } catch (IllegalArgumentException e) {
                return AVAILABLE;
            }
        }
    }
}
