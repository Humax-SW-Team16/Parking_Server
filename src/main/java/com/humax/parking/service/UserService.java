package com.humax.parking.service;

import com.humax.parking.dto.*;
import com.humax.parking.model.ParkingEntity;
import com.humax.parking.repository.UserRepository;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final RedisTemplate<String, Integer> redisTemplate;

    private final HashOperations<String, String, Integer> hashOperations;

    private static final String SEARCH_COUNT_KEY = "parking_search_count";

    private final UserRepository userRepository;

//    private void updateSearchCount(List<ParkingEntity> parkingEntities){
//        for(ParkingEntity parkingEntity : parkingEntities){
//            parkingEntity.setSearchCount(parkingEntity.getSearchCount()+1);
//        }
//    }
    public void increaseParkingSearchCount(String parkingId) {
      String parkingKey = SEARCH_COUNT_KEY + ":" + parkingId;
      Long count = redisTemplate.opsForValue().increment(parkingKey, 1);
      if (count == null || count == 1) {
          // Expire after 24 hours (86400 seconds)
          redisTemplate.expire(parkingKey, 86400, TimeUnit.SECONDS);
    }
}

    // Redis에서 특정 주차장 ID(parkingId)에 대한 검색 횟수를 가져오는 메서드
    public Long getParkingSearchCount(String parkingId) {
        //주차장 검색 횟수를 저장하기 위한 Redis 키의 기본 값
        //이 키는 주차장 ID와 함께 조합하여 특정 주차장에 대한 검색 횟수를 저장하는 데 사용
        String parkingKey = SEARCH_COUNT_KEY + ":" + parkingId;
        Object count = redisTemplate.opsForValue().get(parkingKey);//parkingKey=특정 주차장의 검색 횟수를 저장하는데 사용
        return count != null ? (Long) count : 0L;
    }


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


}
