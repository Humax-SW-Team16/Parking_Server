package com.humax.parking.controller;

import com.humax.parking.service.UserService;
import com.humax.parking.dto.ParkingInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "/api/v1/parking")
public class ParkingMainController {

    private final UserService userService;

    public ParkingMainController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/main")
    public ResponseEntity<List<ParkingInfoDTO>> getParkingInfoForUI(Model model) {
        try {
            // Redis에서 주차장 정보 가져오기
            List<ParkingInfoDTO> parkingInfoList = userService.getParkingInfo();

            // 검색 횟수를 높은 순으로 정렬
            List<ParkingInfoDTO> sortedParkingInfoList = parkingInfoList.stream()
                    .sorted(Comparator.comparingInt(dto -> -userService.getSearchCount(dto.getParkingId())))
                    .collect(Collectors.toList());

            // 메인 페이지에 전달할 데이터 설정
            model.addAttribute("parkingInfoList", sortedParkingInfoList);

            return ResponseEntity.status(HttpStatus.OK).body(sortedParkingInfoList);
        } catch (Exception e) {
            log.error("주차장 정보 목록을 가져오는 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}