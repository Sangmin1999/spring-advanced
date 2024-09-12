package org.example.expert.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.config.JwtUtil;

import java.time.LocalDateTime;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class SpringAspect {

    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;


//    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
//    private void deleteCommentLayer() {}
//    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
//    private void changeUserRoleLayer() {}

    @Pointcut("@annotation(org.example.expert.domain.common.annotation.TrackTime)")
    private void trackTimeAnnotation() {}



    @Before("trackTimeAnnotation()")
    public void logApiMethod() {
        // 요청 헤더에서 Authorization 헤더의 JWT 토큰을 가져옴
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = jwtUtil.substringToken(authHeader);
            Claims claims = jwtUtil.extractClaims(token);

            String userId = claims.getSubject();
            LocalDateTime localDateTime = LocalDateTime.now();
            String requestUrl = request.getRequestURI();

            log.info(userId+ " " + localDateTime+ " " + requestUrl);
        }
    }
}
