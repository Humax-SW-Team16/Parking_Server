package com.humax.parking.service;

import com.humax.parking.common.util.JwtUtil;
import com.humax.parking.dto.*;
import com.humax.parking.model.Bookmark;
import com.humax.parking.model.Enter;
import com.humax.parking.model.ParkingEntity;
import com.humax.parking.model.User;
import com.humax.parking.repository.EnterRepository;
import com.humax.parking.repository.ParkingRepository;
import com.humax.parking.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ParkingRepository parkingRepository;

    private final EnterRepository enterRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private final JwtUtil jwtUtil;
    private static final String SEARCH_COUNT_KEY_PREFIX = "search_count:";

    public LocalDateTime saveEnterTime(String token, Long parkingId, LocalDateTime time){
        Long userId = jwtUtil.getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ParkingEntity parkingEntity = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new RuntimeException("주차장을 찾을 수 없습니다."));

        Enter enter = Enter.builder()
                .user(user)
                .parkingEntity(parkingEntity)
                .entryTime(time)
                .build();
        enterRepository.save(enter);

        return time;
    }

    public LocalDateTime saveOutTime(String token, Long parkingId, LocalDateTime time){
        Long userId = jwtUtil.getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ParkingEntity parkingEntity = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new RuntimeException("주차장을 찾을 수 없습니다."));

        Enter enter1 = enterRepository.findByUser(user);

        enterRepository.updateExitTimeById(time, enter1.getEnterId());

        return time;
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

                parkingInfoDTO.setParkingId(parkingEntity.getParkingId());
                parkingInfoDTO.setName(parkingEntity.getName());
                parkingInfoDTO.setAddress(parkingEntity.getAddress());

                parkingInfoDTO.setLat(parkingEntity.getLat());
                parkingInfoDTO.setLon(parkingEntity.getLon());

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
            parkingDetail.setLat(parkingEntity.getLat());
            parkingDetail.setLon(parkingEntity.getLon());
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


    public void updateSearchCount(List<ParkingEntity> parkingEntities) {
        for (ParkingEntity parkingEntity : parkingEntities) {
            String key = SEARCH_COUNT_KEY_PREFIX + parkingEntity.getParkingId();
            stringRedisTemplate.opsForValue().increment(key);
        }
    }


    //  ParkingEntity를 ParkingInfoDTO로 변환 (stream 적용 안됨)
    private ParkingInfoDTO convertToParkingInfoDTO(ParkingEntity parkingEntity) {
        ParkingInfoDTO parkingInfoDTO = new ParkingInfoDTO();

        parkingInfoDTO.setParkingId(parkingEntity.getParkingId());
        parkingInfoDTO.setName(parkingEntity.getName());
        parkingInfoDTO.setAddress(parkingEntity.getAddress());
        parkingInfoDTO.setLat(parkingEntity.getLat());
        parkingInfoDTO.setLon(parkingEntity.getLon());
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
    public Map<String, String> getAllSearchCounts(){
        Set<String> keys = stringRedisTemplate.keys(SEARCH_COUNT_KEY_PREFIX + "*");

        if(keys != null && !keys.isEmpty()){
            List<String> keyList = new ArrayList<>(keys);
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keyList);

            Map<String, String> searchCounts = new HashMap<>();
            for(int i = 0; i < keyList.size(); i++){
                String key = keyList.get(i);
                String parkingId = key.substring(SEARCH_COUNT_KEY_PREFIX.length());
                String count = values.get(i);
                searchCounts.put(parkingId, count);
            }
            return searchCounts;
        }
        return Collections.emptyMap();
    }

    public int getSearchCount(Long parkingId) {
        String key = SEARCH_COUNT_KEY_PREFIX + parkingId;
        String count = stringRedisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
}
