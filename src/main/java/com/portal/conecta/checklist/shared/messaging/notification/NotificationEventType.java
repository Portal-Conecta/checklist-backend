package main.java.com.portal.conecta.checklist.shared.messaging.notification;

public final class NotificationEventType {

    private NotificationEventType() {} 

    public static final String CHECKLIST_MISSED_DEADLINE = "checklist.missed_deadline";
    public static final String CHECKLIST_THREE_DAYS_MISSING = "checklist.three_days_missing";
    public static final String CHECKLIST_SUBMITTED = "checklist.submitted";
}