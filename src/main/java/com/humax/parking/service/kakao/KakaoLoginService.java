package com.humax.parking.service.kakao;

import com.humax.parking.common.util.JwtUtil;
import com.humax.parking.dto.LoginResultDto;
import com.humax.parking.dto.SocialUserProfileDto;
import com.humax.parking.model.User;
import com.humax.parking.repository.OAuthUserRepository;
import com.humax.parking.service.kakao.ExchangeKakaoAccessToken;
import com.humax.parking.service.kakao.FetchKakaoUserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    //private static final LoginType KAKAO_LOGIN_TYPE = LoginType.KAKAO;

    private final ExchangeKakaoAccessToken exchangeKakaoAccessToken;
    private final FetchKakaoUserProfile fetchKakaoUserProfile;
    private final OAuthUserRepository oAuthUserRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResultDto handleKakaoLogin(String authorizationCode) {
        String accessToken = exchangeKakaoAccessToken(authorizationCode);
        SocialUserProfileDto userProfile = fetchKakaoUserProfile(accessToken);

        Optional<User> foundUser = oAuthUserRepository.findByEmail(userProfile.getEmail());
        boolean isNewUser = foundUser.isEmpty();

        User user = foundUser.orElseGet(() -> registerUser(userProfile));

        String token = createToken(user);
        return new LoginResultDto(token, isNewUser);
    }

    private String exchangeKakaoAccessToken(String authorizationCode) {
        return exchangeKakaoAccessToken.doExchange(authorizationCode);
    }

    private SocialUserProfileDto fetchKakaoUserProfile(String accessToken) {
        return fetchKakaoUserProfile.doFetch(accessToken);
    }

    private String createToken(User user) {
        return jwtUtil.createToken(user.getUserId());
    }

    private User registerUser(SocialUserProfileDto userProfile) {
        User user = new User(
                userProfile.getNickname(),
                userProfile.getProfileImageUrl(),
                userProfile.getEmail()); //로그인 타입 제거

        return oAuthUserRepository.save(user);
    }
}