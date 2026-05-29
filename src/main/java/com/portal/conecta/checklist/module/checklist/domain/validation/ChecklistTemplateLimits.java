package com.portal.conecta.checklist.module.checklist.domain.validation;

public final class ChecklistTemplateLimits {

    public static final int MAX_SECTIONS = 20;
    public static final int MAX_ITEMS_PER_SECTION = 50;
    public static final int MAX_TOTAL_ITEMS = 500;
    public static final long MAX_REQUEST_BODY_BYTES = 1_048_576L;
    public static final String MAX_REQUEST_BODY_DISPLAY = "1 MiB";

    private ChecklistTemplateLimits() {
    }
}
