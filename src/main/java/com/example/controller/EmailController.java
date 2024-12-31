package com.example.controller;

import com.example.entity.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EmailController {

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody EmailRequest request) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

//            SimpleMailMessage message = new SimpleMailMessage();
            helper.setFrom("wangheng0411@gmail.com");
            helper.setTo(request.getTargetMail());
            helper.setSubject(request.getTitle());
            helper.setText(request.getContent());

            emailSender.send(message);
            return "Email sent successfully";
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}
