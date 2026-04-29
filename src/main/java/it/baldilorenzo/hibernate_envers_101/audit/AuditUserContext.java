package it.baldilorenzo.hibernate_envers_101.audit;

public final class AuditUserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    private AuditUserContext() {}

    public static void setUser(String user) {
        CURRENT_USER.set(user);
    }

    public static String getUser() {
        String user = CURRENT_USER.get();
        return user != null ? user : "system";
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
