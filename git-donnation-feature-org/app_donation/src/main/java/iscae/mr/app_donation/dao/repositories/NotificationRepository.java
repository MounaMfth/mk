package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    long countByUserIdAndIsReadFalse(Long userId);
}



