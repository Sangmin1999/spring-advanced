package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입_성공() {
        // given

        SignupRequest signupRequest = new SignupRequest("test@example.com", "password", "User");
        UserRole userRole = UserRole.USER;
        User realUser = new User(signupRequest.getEmail(), "encodedPassword", userRole);
        User spyUser = spy(realUser);

        given(spyUser.getId()).willReturn(1L);

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(spyUser);

        given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn("token");
        // when

        SignupResponse signupResponse = authService.signup(signupRequest);
        // then

        assertNotNull(signupResponse);
        assertEquals("token", signupResponse.getBearerToken());
        verify(userRepository).save(any(User.class));

    }

    @Test
    void 중복된_이메일로_회원가입_실패() {
        // given

        SignupRequest signupRequest = new SignupRequest("test@example.com", "password", "User");

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when / then

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }
}
