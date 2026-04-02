package com.negzaoui.stuffing.repository;

import com.negzaoui.stuffing.entity.Notification;
import com.negzaoui.stuffing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Notifications non lues d'un utilisateur précis */
    List<Notification> findByTargetUserAndIsReadFalseOrderByCreatedAtDesc(User targetUser);

    /** Toutes les notifications d'un utilisateur */
    List<Notification> findByTargetUserOrderByCreatedAtDesc(User targetUser);

    /** Nombre de non lues pour un utilisateur */
    long countByTargetUserAndIsReadFalse(User targetUser);

    /** Notifications globales non lues (sans cible, broadcast) */
    List<Notification> findByTargetUserIsNullAndIsReadFalseOrderByCreatedAtDesc();

    /** Ancienne méthode (gardée pour compatibilité) */
    List<Notification> findByIsReadFalse();

    long countByIsReadFalse();

}
