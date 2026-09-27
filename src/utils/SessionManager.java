package utils;

import model.Crew;
import model.Enums.UserRole;
import model.User;

/**
 * Manages the current logged-in user session.
 */
public class SessionManager {

    private static User currentUser;
    private static Crew currentCrew;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized void setCurrentCrew(Crew crew) {
        currentCrew = crew;
    }

    public static synchronized Crew getCurrentCrew() {
        return currentCrew;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public static synchronized void logout() {
        currentUser = null;
        currentCrew = null;
    }
}
