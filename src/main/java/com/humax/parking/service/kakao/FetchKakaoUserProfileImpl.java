package com.humax.parking.service.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humax.parking.common.exception.CustomException;
import com.humax.parking.dto.SocialUserProfileDto;
import com.humax.parking.service.kakao.FetchKakaoUserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//이 클래스는 주어진 카카오 액세스 토큰을 사용하여 카카오 사용자 프로필 정보를 가져오는 역할을 합니다.
@Component
public class FetchKakaoUserProfileImpl implements FetchKakaoUserProfile {

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String endPoint;

    public SocialUserProfileDto doFetch(String accessToken) {
        try {
            return fetchUserProfile(accessToken);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 사용자 프로필 오류");
        }
    }

    private SocialUserProfileDto fetchUserProfile(String accessToken)
            throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // 여기 포함되어 있는 Authorization은 사용자 정보를 가져오는 url에서만 보임
        HttpEntity<Void> httpEntity = createProfileHttpEntity(accessToken);

        // 이 응답에는 사용자의 프로필 정보가 담겨있음
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                endPoint,
                HttpMethod.GET,
                httpEntity,
                String.class);

        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());

        String nickname = parseNickname(jsonNode);
        String email = parseEmail(jsonNode);
        String profileImageUrl = parseProfileImageUrl(jsonNode);

        return new SocialUserProfileDto(nickname, email, profileImageUrl);
    }

    private HttpEntity<Void> createProfileHttpEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        return new HttpEntity<>(headers);
    }

    private String parseNickname(JsonNode jsonNode) {
        return jsonNode.get("kakao_account")
                .get("profile")
                .get("nickname")
                .asText();
    }

    private String parseEmail(JsonNode jsonNode) {
        return jsonNode.get("kakao_account")
                .get("email")
                .asText();
    }

    private String parseProfileImageUrl(JsonNode jsonNode) {
        return jsonNode.get("kakao_account")
                .get("profile")
                .get("profile_image_url")
                .asText();
    }
}