package com.humax.parking.service;

import com.humax.parking.model.Bookmark;
import com.humax.parking.model.ParkingEntity;
import com.humax.parking.model.User;
import com.humax.parking.repository.BookmarkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void addBookmark(User user, ParkingEntity parkingEntity) {
        if (isBookmarked(user, parkingEntity)) {
            throw new RuntimeException("이미 찜한 주차장입니다.");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .parkingEntity(parkingEntity)
                .build();

        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(User user, ParkingEntity parkingEntity) {
        Bookmark bookmark = bookmarkRepository.findByUserAndParkingEntity(user, parkingEntity)
                .orElseThrow(() -> new RuntimeException("찜하지 않은 주차장입니다."));

        bookmarkRepository.delete(bookmark);
    }

    public boolean isBookmarked(User user, ParkingEntity parkingEntity) {
        return bookmarkRepository.existsByUserAndParkingEntity(user, parkingEntity);
    }

    public List<Bookmark> getBookmarksByUser(User user) {
        return bookmarkRepository.findByUser(user);
    }
}

