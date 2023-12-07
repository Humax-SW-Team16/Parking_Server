package com.humax.parking.service.redis;

import com.humax.parking.service.UserService;
import java.util.ArrayList;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class SearchCountUpdateTask {

    private final UserService userService;

    public SearchCountUpdateTask(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(fixedDelay = 60000) // 1분마다 실행 (밀리초 단위) -> 추후 실제 배치 작업 시 1시간으로 변갱
    public void updateSearchCount() {
        try {
            // 여기서 userService의 메소드를 호출하여 MySQL의 검색 횟수를 갱신
            userService.updateSearchCount(new ArrayList<>());
            System.out.println("갱신 완료");
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
        }
    }
}
