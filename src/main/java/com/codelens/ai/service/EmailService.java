package com.codelens.ai.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CodeLens AI - Email Verification");
        message.setText("Hello,\n\n" +
                "Your CodeLens AI verification code is:\n" +
                otp + "\n\n" +
                "This OTP is valid for 5 minutes.\n" +
                "If you did not create this account, you can ignore this email.\n\n" +
                "Regards,\nCodeLens AI Team");

        mailSender.send(message);
    }
}
