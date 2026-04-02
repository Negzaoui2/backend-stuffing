package com.negzaoui.stuffing.service;

import com.negzaoui.stuffing.dto.auth.AccountCreationRequestDto;
import com.negzaoui.stuffing.entity.AccountCreationRequest;
import com.negzaoui.stuffing.entity.AccountRequestStatus;
import com.negzaoui.stuffing.entity.Role;
import com.negzaoui.stuffing.entity.User;
import com.negzaoui.stuffing.repository.AccountCreationRequestRepository;
import com.negzaoui.stuffing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountCreationRequestService {

    private final AccountCreationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * Soumission d'une nouvelle demande (si une PENDING existe déjà pour le même email, on évite un doublon).
     */
    @Transactional
    public void submit(AccountCreationRequestDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        if (requestRepository.existsByEmailAndStatus(dto.getEmail(), AccountRequestStatus.PENDING)) {
            // On ne crée pas de doublon de demande en attente
            return;
        }

        var req = AccountCreationRequest.builder()
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .company(dto.getCompany())
                .jobTitle(dto.getJobTitle())
                .message(dto.getMessage())
                .status(AccountRequestStatus.PENDING)
                .build();

        requestRepository.save(req);

        // Notifier tous les ADMIN qu'une nouvelle demande est arrivée
        notificationService.notifyAdmins(
                "Nouvelle demande de création de compte de " + dto.getFirstName() + " " + dto.getLastName() + " (" + dto.getEmail() + ")",
                "ACCOUNT_REQUEST"
        );

        // Notification basique (log/envoyer mail selon ta conf)
       // emailService.sendAccountRequestReceived(dto.getEmail());
    }

    /**
     * LIST : Toutes les demandes (triées DESC) ou filtrées par statut si fourni.
     * Utilisée par le contrôleur Variante A : GET /api/hr/account-requests?status=...
     */
    @Transactional(readOnly = true)
    public Page<AccountCreationRequest> list(AccountRequestStatus status, Pageable pageable) {
        if (status == null) {
            return requestRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return requestRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * APPROVE : Crée (ou réutilise) un utilisateur et marque la demande APPROVED.
     * La demande est conservée pour traçabilité (processedAt / processedBy).
     */
    @Transactional
    public User approve(Long requestId, Role role, String temporaryPassword, Authentication processedBy) {
        var req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        if (req.getStatus() != AccountRequestStatus.PENDING) {
            throw new IllegalStateException("Demande déjà traitée");
        }

        // Si le compte existe déjà, on valide la demande sans recréer l'utilisateur.
        if (userRepository.existsByEmail(req.getEmail())) {
            req.setStatus(AccountRequestStatus.APPROVED);
            req.setProcessedAt(Instant.now());
            req.setProcessedBy(processedBy != null ? processedBy.getName() : null);
            requestRepository.save(req);
            return userRepository.findByEmail(req.getEmail()).orElseThrow();
        }

        String rawPassword = (temporaryPassword == null || temporaryPassword.isBlank())
                ? generateTempPassword()
                : temporaryPassword;

        var user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        userRepository.save(user);

        // Mettre à jour la demande pour garder l'historique
        req.setStatus(AccountRequestStatus.APPROVED);
        req.setProcessedAt(Instant.now());
        req.setProcessedBy(processedBy != null ? processedBy.getName() : null);
        requestRepository.save(req);

        // Notifier l'utilisateur avec son mot de passe (ou log selon ta conf)
        emailService.sendAccountApprovedEmail(user.getEmail(), rawPassword);

        // Notification in-app pour le nouveau utilisateur
        notificationService.createNotification(
                "Bienvenue ! Votre compte a été créé avec succès.",
                "ACCOUNT_APPROVED",
                user
        );

        return user;
    }

    /**
     * REJECT : Marque la demande REJECTED et conserve la trace (processedAt / processedBy).
     */
    @Transactional
    public void reject(Long requestId, String reason, Authentication processedBy) {
        var req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        if (req.getStatus() != AccountRequestStatus.PENDING) {
            throw new IllegalStateException("Demande déjà traitée");
        }

        req.setStatus(AccountRequestStatus.REJECTED);
        req.setProcessedAt(Instant.now());
        req.setProcessedBy(processedBy != null ? processedBy.getName() : null);
        requestRepository.save(req);

        emailService.sendAccountRejectedEmail(req.getEmail(), reason);
    }

    // ---------------------
    // Utils
    // ---------------------

    private String generateTempPassword() {
        // Simple pour PFE. En prod : politique de complexité configurable.
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#-";
        SecureRandom rnd = new SecureRandom();
        int len = 12;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}