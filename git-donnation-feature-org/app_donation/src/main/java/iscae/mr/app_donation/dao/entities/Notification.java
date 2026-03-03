package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId; // User who receives the notification
    
    @Column(nullable = false, length = 100)
    private String type; // BUDGET_REACHED, PROGRESS_UPDATE, PROJECT_START, ACTIVITY_START, DONATION_RECEIVED
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Column(length = 50)
    private String relatedEntityType; // PROJET, ACTIVITE, DON
    
    @Column(length = 100)
    private String relatedEntityId; // ID of the related entity
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}



