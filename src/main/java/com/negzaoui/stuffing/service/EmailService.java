package com.negzaoui.stuffing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi d'emails.
 *
 * Mode PFE/dev: si app.mail.enabled=false, on log le contenu au lieu d'envoyer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@stuffing.local}")
    private String from;

    private JavaMailSender getMailSenderOrNull() {
        return mailSenderProvider.getIfAvailable();
    }

    public void sendPasswordResetEmail(String to, String resetLinkOrToken) {
        String subject = "Réinitialisation de mot de passe";
        String body = "Bonjour,\n\n" +
                "Voici votre lien (ou token) pour réinitialiser votre mot de passe :\n" +
                resetLinkOrToken + "\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.";

        if (!mailEnabled) {
            // Dev/PFE: pas de SMTP configuré, on affiche dans les logs
            log.warn("[MAIL DISABLED] To={}, Subject={}, Body=\n{}", to, subject, body);
            return;
        }

        JavaMailSender mailSender = getMailSenderOrNull();
        if (mailSender == null) {
            log.warn("[MAIL NOT CONFIGURED] Impossible d'envoyer un email (JavaMailSender absent). To={}, Subject={}", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendAccountRequestReceived(String requesterEmail) {
        String subject = "Demande de création de compte reçue";
        String body = "Bonjour,\n\n" +
                "Votre demande de création de compte a été reçue et sera traitée par l'administrateur.\n" +
                "Vous serez contacté(e) dès que votre compte sera créé.\n\n" +
                "Email demandé: " + requesterEmail + "\n";

        if (!mailEnabled) {
            log.warn("[MAIL DISABLED] To={}, Subject={}, Body=\n{}", requesterEmail, subject, body);
            return;
        }

        JavaMailSender mailSender = getMailSenderOrNull();
        if (mailSender == null) {
            log.warn("[MAIL NOT CONFIGURED] Impossible d'envoyer un email (JavaMailSender absent). To={}, Subject={}", requesterEmail, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(requesterEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendAccountApprovedEmail(String to, String temporaryPassword) {
        String subject = "Votre compte a été créé";
        String body = "Bonjour,\n\n" +
                "Votre compte a été créé. Voici vos informations de connexion :\n" +
                "Email: " + to + "\n" +
                "Mot de passe temporaire: " + temporaryPassword + "\n\n" +
                "Nous vous recommandons de changer votre mot de passe après la première connexion.";

        if (!mailEnabled) {
            log.warn("[MAIL DISABLED] To={}, Subject={}, Body=\n{}", to, subject, body);
            return;
        }

        JavaMailSender mailSender = getMailSenderOrNull();
        if (mailSender == null) {
            log.warn("[MAIL NOT CONFIGURED] Impossible d'envoyer un email (JavaMailSender absent). To={}, Subject={}", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendAccountRejectedEmail(String to, String reason) {
        String subject = "Demande de création de compte";
        String body = "Bonjour,\n\n" +
                "Votre demande de création de compte a été rejetée." +
                (reason == null || reason.isBlank() ? "" : ("\nRaison: " + reason)) +
                "\n\nSi vous pensez qu'il s'agit d'une erreur, veuillez contacter l'administrateur.";

        if (!mailEnabled) {
            log.warn("[MAIL DISABLED] To={}, Subject={}, Body=\n{}", to, subject, body);
            return;
        }

        JavaMailSender mailSender = getMailSenderOrNull();
        if (mailSender == null) {
            log.warn("[MAIL NOT CONFIGURED] Impossible d'envoyer un email (JavaMailSender absent). To={}, Subject={}", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
