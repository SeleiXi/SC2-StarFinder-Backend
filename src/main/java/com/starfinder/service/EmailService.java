package com.starfinder.service;

public interface EmailService {
    /**
     * Sends a verification code to the specified email address.
     * @param email The target email address
     * @param code The 6-digit verification code
     * @return true if successful, false otherwise
     */
    boolean sendVerificationCode(String email, String code);

    /**
     * Verifies the code for the specified email address.
     * @param email The target email address
     * @param code The code to verify
     * @return true if the code is valid and not expired, false otherwise
     */
    boolean verifyCode(String email, String code);
}
