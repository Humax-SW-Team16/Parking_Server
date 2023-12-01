package com.humax.parking.service.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humax.parking.common.exception.CustomException;
import com.humax.parking.service.kakao.ExchangeKakaoAccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

//이 클래스는 주어진 카카오 인증 코드를 사용하여 카카오로부터 액세스 토큰을 교환하는 역할을 합니다.
@Slf4j
@Component
public class ExchangeKakaoAccessTokenImpl implements ExchangeKakaoAccessToken {
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String restApiKey;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectURI;

    @Value("${spring.security.oauth2.client.provider.kakao.token_uri}")
    private String endPoint;

    @Override
    public String doExchange(String authorizationCode) {
        try {
            return exchangeAccessToken(authorizationCode);
        } catch (IOException e) {
            log.error("카카오 액세스 토큰 교환 오류");
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인 실패");
        }
    }

    private String exchangeAccessToken(String authorizationCode) throws JsonProcessingException {
        HttpEntity<MultiValueMap<String, String>> httpEntity = createLoginHttpEntity(
                authorizationCode);
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                endPoint,
                HttpMethod.POST,
                httpEntity,
                String.class);

        if (responseEntity.getBody() == null || responseEntity.getBody().isBlank()) {
            return "";
        }

        return objectMapper.readTree(responseEntity.getBody())
                .get("access_token")
                .asText();
    }

    private HttpEntity<MultiValueMap<String, String>> createLoginHttpEntity(
            String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", restApiKey);
        body.add("redirect_uri", redirectURI);
        body.add("code", authorizationCode);
        body.add("client_secret", clientSecret);

        return new HttpEntity<>(body, headers);
    }
}
