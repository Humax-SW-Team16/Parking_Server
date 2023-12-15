package com.humax.parking.controller;


import com.humax.parking.dto.LoginResultDto;
import com.humax.parking.service.kakao.KakaoLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OauthController {

    private final KakaoLoginService kakaoLoginService;

    //@Value("${kakao.login-url}
    //private String kakaoLoginUri;

//    @Value("${MAIN_PAGE_URL}")
//    private String mainPageUrl;
//
//    @Value("${MY_PAGE_URL}")
//    private String myPageUrl;

    //클라이언트가 해당 엔드포인트에 접근하면, kakaoLoginUri로 리디렉션하여 사용자를 카카오 로그인 페이지로 보냅니다.
    @GetMapping("/kakao/login")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=9f5309f7fc6b371a2a96d9cfdbd304cd&redirect_uri=https://www.turu-parking.com/oauth/kakao/login&response_type=code");
    }

    //카카오 소셜 로그인 구현
    @GetMapping("/oauth/kakao/login")
    public void kakaoLogin(@RequestParam(name = "code", required = false) String authCode, HttpServletResponse response)
            throws IOException {

        LoginResultDto loginResult = kakaoLoginService.handleKakaoLogin(authCode);
        boolean isNewUser = loginResult.isNewUser();

        Cookie authorization = new Cookie("Authorization", loginResult.getToken());
        authorization.setSecure(false); // HTTPS 연결에서만 쿠키 전송 localhost에서는 허용됨
        authorization.setHttpOnly(true); // JavaScript에서 접근 불가
        authorization.setPath("/"); // 전체 경로에 대해 쿠키 적용
        authorization.setMaxAge(3600); // 1시간 동안 유효
        response.addCookie(authorization);

        //String redirectUrl = isNewUser? myPageUrl : mainPageUrl;
        String redirectUrl = "/api/v1/parking/main";
        response.sendRedirect(redirectUrl);
    }
}
