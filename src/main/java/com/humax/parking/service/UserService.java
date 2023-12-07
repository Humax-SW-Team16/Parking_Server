package com.humax.parking.service;

import com.humax.parking.dto.*;
import com.humax.parking.model.ParkingEntity;
import com.humax.parking.repository.ParkingRepository;
import com.humax.parking.repository.UserRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ParkingRepository parkingRepository;

    private final StringRedisTemplate stringRedisTemplate;

    private static final String SEARCH_COUNT_KEY_PREFIX = "search_count:";

    public List<ParkingInfoDTO> findNearbyParking(UserLocationDTO userLocationDTO){
        try{
            double userLatitude = Double.parseDouble(userLocationDTO.getLat());
            double userLongitude = Double.parseDouble(userLocationDTO.getLon());
            int maxDistance = userLocationDTO.getDistance();

            List<ParkingEntity> nearParkingEntities = userRepository.findNearbyParking(
                    userLatitude, userLongitude, maxDistance);

            List<ParkingInfoDTO> parkingInfoDTOs = new ArrayList<>();
            for (ParkingEntity parkingEntity : nearParkingEntities) {
                ParkingInfoDTO parkingInfoDTO = new ParkingInfoDTO();

                parkingInfoDTO.setParkingId(parkingEntity.getParkingId());
                parkingInfoDTO.setName(parkingEntity.getName());
                parkingInfoDTO.setAddress(parkingEntity.getAddress());
                parkingInfoDTO.setOperatingTime(parkingEntity.getOperatingTime());
                parkingInfoDTO.setTimeTicket(parkingEntity.getTimeTicket());
                parkingInfoDTO.setNormalSeason(parkingEntity.getNormalSeason());
                parkingInfoDTO.setTenantSeason(parkingEntity.getTenantSeason());
                parkingInfoDTO.setDayTicket(parkingEntity.getDayTicket());
                parkingInfoDTO.setSpecialDay(parkingEntity.getSpecialDay());
                parkingInfoDTO.setSpecialHour(parkingEntity.getSpecialHour());
                parkingInfoDTO.setSpecialNight(parkingEntity.getSpecialNight());
                parkingInfoDTO.setSpecialWeekend(parkingEntity.getSpecialWeekend());
                parkingInfoDTO.setApplyDay(parkingEntity.getApplyDay());
                parkingInfoDTO.setApplyHour(parkingEntity.getApplyHour());
                parkingInfoDTO.setApplyNight(parkingEntity.getApplyNight());
                parkingInfoDTO.setApplyWeekend(parkingEntity.getApplyWeekend());
                parkingInfoDTOs.add(parkingInfoDTO);
            }

            // 검색 횟수 갱신
            updateSearchCount(nearParkingEntities);

            return parkingInfoDTOs;
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("가까운 주차장을 찾지 못했습니다.", e);
        }
    }

    public List<ParkingInfoDTO> getParkingInfo() {
        try {
            List<ParkingEntity> parkingEntities = parkingRepository.findAll();
            return parkingEntities.stream()
                    .map(this::convertToParkingInfoDTO)  // 변환 메서드 호출
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("주차장 정보 목록을 가져오는 중 오류 발생", e);
        }
    }

    public ParkingInfoDTO getParkingDetail(Long parkingId) {
        try {
            ParkingEntity parkingEntity = parkingRepository.findById(parkingId)
                    .orElseThrow(() -> new RuntimeException(parkingId + "번 주차장을 찾을 수 없습니다."));

            ParkingInfoDTO parkingDetail = new ParkingInfoDTO();
            parkingDetail.setParkingId(parkingEntity.getParkingId());
            parkingDetail.setName(parkingEntity.getName());
            parkingDetail.setAddress(parkingEntity.getAddress());
            parkingDetail.setOperatingTime(parkingEntity.getOperatingTime());
            parkingDetail.setTimeTicket(parkingEntity.getTimeTicket());
            parkingDetail.setNormalSeason(parkingEntity.getNormalSeason());
            parkingDetail.setTenantSeason(parkingEntity.getTenantSeason());
            parkingDetail.setDayTicket(parkingEntity.getDayTicket());
            parkingDetail.setSpecialDay(parkingEntity.getSpecialDay());
            parkingDetail.setSpecialHour(parkingEntity.getSpecialHour());
            parkingDetail.setSpecialNight(parkingEntity.getSpecialNight());
            parkingDetail.setSpecialWeekend(parkingEntity.getSpecialWeekend());
            parkingDetail.setApplyDay(parkingEntity.getApplyDay());
            parkingDetail.setApplyHour(parkingEntity.getApplyHour());
            parkingDetail.setApplyNight(parkingEntity.getApplyNight());
            parkingDetail.setApplyWeekend(parkingEntity.getApplyWeekend());

            return parkingDetail;
        } catch (Exception e) {
            throw new RuntimeException("주차장 상세 정보를 가져오는 중 오류 발생", e);
        }
    }


    private void updateSearchCount(List<ParkingEntity> parkingEntities) {
        for (ParkingEntity parkingEntity : parkingEntities) {
            parkingEntity.setSearchCount(parkingEntity.getSearchCount() + 1);
        }
    }



    // // ParkingEntity를 ParkingInfoDTO로 변환 (stream 적용 안됨)
    private ParkingInfoDTO convertToParkingInfoDTO(ParkingEntity parkingEntity) {
        ParkingInfoDTO parkingInfoDTO = new ParkingInfoDTO();

        parkingInfoDTO.setParkingId(parkingEntity.getParkingId());
        parkingInfoDTO.setName(parkingEntity.getName());
        parkingInfoDTO.setAddress(parkingEntity.getAddress());
        parkingInfoDTO.setOperatingTime(parkingEntity.getOperatingTime());
        parkingInfoDTO.setTimeTicket(parkingEntity.getTimeTicket());
        parkingInfoDTO.setNormalSeason(parkingEntity.getNormalSeason());
        parkingInfoDTO.setTenantSeason(parkingEntity.getTenantSeason());
        parkingInfoDTO.setDayTicket(parkingEntity.getDayTicket());
        parkingInfoDTO.setSpecialDay(parkingEntity.getSpecialDay());
        parkingInfoDTO.setSpecialHour(parkingEntity.getSpecialHour());
        parkingInfoDTO.setSpecialNight(parkingEntity.getSpecialNight());
        parkingInfoDTO.setSpecialWeekend(parkingEntity.getSpecialWeekend());
        parkingInfoDTO.setApplyDay(parkingEntity.getApplyDay());
        parkingInfoDTO.setApplyHour(parkingEntity.getApplyHour());
        parkingInfoDTO.setApplyNight(parkingEntity.getApplyNight());
        parkingInfoDTO.setApplyWeekend(parkingEntity.getApplyWeekend());

        return parkingInfoDTO;
    }
}
