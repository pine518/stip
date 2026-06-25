package com.stip.common.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class TraceIds {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private TraceIds() {
    }

    public static String current() {
        return "trace-" + DATE.format(LocalDate.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

