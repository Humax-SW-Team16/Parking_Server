package com.humax.parking.repository;

import com.humax.parking.model.Bookmark;
import com.humax.parking.model.Enter;
import com.humax.parking.model.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface EnterRepository extends JpaRepository<Enter, Long> {
    Enter findByUser(User user);

    @Modifying
    @Query("UPDATE Enter e SET e.exitTime = :exitTime WHERE e.id = :id")
    void updateExitTimeById(@Param("exitTime") LocalDateTime exitTime, @Param("id") Long id);
}
