package com.BankApp.localbankapp.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Brazhkin
 */
public class AccountNumberGenerator {
    private static final AtomicLong sequence = new AtomicLong(10000000);
    private static final String BANK_CODE = "LBA";

    public static String generate() {
        long nextNum = sequence.incrementAndGet();
        return BANK_CODE + String.format("%010d", nextNum);
    }
}