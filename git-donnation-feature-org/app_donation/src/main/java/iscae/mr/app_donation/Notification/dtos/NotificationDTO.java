package iscae.mr.app_donation.Notification.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String type;
    private String message;
    private String relatedEntityType;
    private String relatedEntityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}



