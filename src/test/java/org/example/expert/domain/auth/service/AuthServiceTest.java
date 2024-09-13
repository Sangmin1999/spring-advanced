package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void 회원가입_성공() {
        // given

        SignupRequest signupRequest = new SignupRequest("test@example.com", "password", "User");
        UserRole userRole = UserRole.USER;
        User realUser = new User(signupRequest.getEmail(), "encodedPassword", userRole);
        User spyUser = spy(realUser);

        given(spyUser.getId()).willReturn(1L);

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(spyUser);

        given(jwtUtil.createToken(anyLong(),anyString(),any(UserRole.class))).willReturn("token");
        // when

        SignupResponse signupResponse = authService.signup(signupRequest);
        // then

        assertNotNull(signupResponse);
        assertEquals("token", signupResponse.getBearerToken());
        verify(userRepository).save(any(User.class));

    }
}
