package com.cheerup.cheerup.service;

import com.cheerup.cheerup.dto.SignupRequestDto;
import com.cheerup.cheerup.model.User;
import com.cheerup.cheerup.model.UserRole;
import com.cheerup.cheerup.repository.UserRepository;
import com.cheerup.cheerup.security.UserDetailsImpl;
import com.cheerup.cheerup.security.kakao.KakaoOAuth2;
import com.cheerup.cheerup.security.kakao.KakaoUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final KakaoOAuth2 kakaoOAuth2;
    private final AuthenticationManager authenticationManager;
    private static final String ADMIN_TOKEN = "AAABnv/xRVklrnYxKZ0aHgTBcXukeZygoC";

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, KakaoOAuth2 kakaoOAuth2, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kakaoOAuth2 = kakaoOAuth2;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void registerUser(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();
        String passwordChecker = requestDto.getPasswordChecker();

        Optional<User> found = userRepository.findByUsername(username);
        if (username.equals("") || password.equals("") || passwordChecker.equals("")) {
            throw new IllegalArgumentException("username || password || passwordChecker??? ??????????????????.");
        } else if (password.length() < 4) {
            throw new IllegalArgumentException("password??? ?????? 4???????????????.");
        } else if (!password.equals(passwordChecker)) {
            throw new IllegalArgumentException("password??? passwordChecker??? ????????????.");
        } else if (found.isPresent()) {
            throw new IllegalArgumentException("????????? ????????? ID??? ???????????????.");
        }
        password = passwordEncoder.encode(requestDto.getPassword());
        UserRole role = UserRole.USER;
        User user = new User(username, password, role);
        userRepository.save(user);
    }

    public void kakaoLogin(String authorizedCode) {
        // ????????? OAuth2 ??? ?????? ????????? ????????? ?????? ??????
        KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(authorizedCode);
        Long kakaoId = userInfo.getId();
        String nickname = userInfo.getNickname();

        // ?????? DB ?????? ?????? Id ??? ????????????
        // ?????? Id = ????????? nickname
        String username = nickname;
        // ???????????? = ????????? Id + ADMIN TOKEN
        String password = kakaoId + ADMIN_TOKEN;

        // DB ??? ????????? Kakao Id ??? ????????? ??????
        User kakaoUser = userRepository.findByKakaoId(kakaoId)
                .orElse(null);

        // ????????? ????????? ????????????
        if (kakaoUser == null) {
            // ???????????? ?????????
            String encodedPassword = passwordEncoder.encode(password);
            // ROLE = ?????????
            UserRole role = UserRole.USER;

            kakaoUser = new User(nickname, encodedPassword, role, kakaoId);
            userRepository.save(kakaoUser);
        }

        // ????????? ??????
        Authentication kakaoUsernamePassword = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(kakaoUsernamePassword);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public Map<String, String> userSession(HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, String> session = new HashMap<>();
        String username = "null";
        String role = "null";
        Optional<UserDetailsImpl> userDetailsOptional = Optional.ofNullable(userDetails);
        if (userDetailsOptional.isPresent()) {
            username = userDetails.getUsername();
            role = String.valueOf(userDetails.getRole());
        }
        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("role", role);
        request.getSession().setMaxInactiveInterval(360 * 60);
        session.put("username", String.valueOf(request.getSession().getAttribute("username")));
        session.put("role", String.valueOf(request.getSession().getAttribute("role")));
        return session;
    }
}