package iscae.mr.app_donation.Notification.controllers;

import iscae.mr.app_donation.Notification.dtos.NotificationDTO;
import iscae.mr.app_donation.Notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get current user ID from JWT token
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    /**
     * Get all notifications for current user
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        System.out.println("🔔 [NotificationController] Getting notifications for userId: " + userId);
        if (userId == null) {
            System.out.println("❌ [NotificationController] userId is null!");
            return ResponseEntity.badRequest().build();
        }
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        System.out.println("📨 [NotificationController] Found " + notifications.size() + " notifications for user " + userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for current user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        System.out.println("🔔 [NotificationController] Getting unread count for userId: " + userId);
        if (userId == null) {
            System.out.println("❌ [NotificationController] userId is null!");
            return ResponseEntity.badRequest().build();
        }
        long count = notificationService.getUnreadCount(userId);
        System.out.println("📨 [NotificationController] Unread count for user " + userId + ": " + count);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read for current user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}

