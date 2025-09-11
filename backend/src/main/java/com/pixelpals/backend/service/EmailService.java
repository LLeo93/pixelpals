package com.pixelpals.backend.service;
import com.pixelpals.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    public void sendVerificationEmail(User user, String verificationToken) {
        try {
            String subject = "Conferma la tua email";
            String verificationLink = "https://pixelpals-pous.onrender.com/verify?token=" + verificationToken;
            String content = "Ciao " + user.getUsername() + ",\n\n"
                    + "Conferma il tuo account cliccando il link:\n"
                    + verificationLink + "\n\n"
                    + "Grazie da PixelPals Team";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile inviare l'email di verifica.", e);
        }
    }
}
