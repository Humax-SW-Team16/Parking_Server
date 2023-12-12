package com.humax.parking.repository;

import com.humax.parking.model.Bookmark;
import com.humax.parking.model.ParkingEntity;
import com.humax.parking.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndParkingEntity(User user, ParkingEntity parkingEntity);
    List<Bookmark> findByUser(User user);
    boolean existsByUserAndParkingEntity(User user, ParkingEntity parkingEntity);
}

