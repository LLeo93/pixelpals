package com.pixelpals.backend.service;

import com.pixelpals.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Importa Value
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Inietta l'email del mittente dalle proprietà (es. spring.mail.username)
    // Assicurati che questa proprietà sia definita in application.properties o env.properties
    @Value("${spring.mail.username}") // Utilizza spring.mail.username come mittente
    private String fromEmail; // Rinominato per chiarezza

    // Modificato per accettare il token
    public void sendVerificationEmail(User user, String verificationToken) {
        // Aggiungi un blocco try-catch per catturare e loggare eventuali errori di invio email
        try {
            String subject = "Conferma la tua email";
            // Costruisci il link di verifica usando il token effettivo
            String verificationLink = "http://localhost:3000/verify?token=" + verificationToken; // Usa il token passato

            String content = "Ciao " + user.getUsername() + ",\n\n"
                    + "Conferma il tuo account cliccando il link:\n"
                    + verificationLink + "\n\n"
                    + "Grazie da PixelPals Team";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // Usa la variabile iniettata
            message.setTo(user.getEmail()); // Assicurati che user.getEmail() non sia null
            message.setSubject(subject);
            message.setText(content);
            System.out.println("Verification Link: " + verificationLink);
            mailSender.send(message);
            System.out.println("Email di verifica inviata a: " + user.getEmail());
        } catch (Exception e) {
            // Logga l'errore in modo più dettagliato
            System.err.println("Errore durante l'invio dell'email di verifica a " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace(); // Stampa lo stack trace completo per debugging
            // Decidi qui se vuoi rilanciare l'eccezione per far fallire la registrazione
            // O se vuoi semplicemente loggare e permettere la registrazione (meno sicuro)
            throw new RuntimeException("Impossibile inviare l'email di verifica.", e);
        }
    }
}
