package org.example.expert.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

@Aspect // AOP 기능을 하는 클래스임을 명시하는 어노테이션
@Component
@Slf4j
@RequiredArgsConstructor
public class MyLoggingAspectHandler {

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    // AOP의 진입 지점
    @Around(
            "execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))"
    )
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable{
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        Long userId = (Long) request.getAttribute("userId");// 사용자 식별자
        LocalDateTime requestTime = LocalDateTime.now();
        // extractRequestBody는 아래에서 설명
        Object requestBody = extractRequestBody(joinPoint);

        log.info("ADMIN API REQUEST: userId={}, time={}, method={}, url={}, body={}",
                userId, requestTime, method, requestURI, toJson(requestBody));
        // 실제로 해당 메서드를 실행하는 코드 이걸 기준으로 실행 전 실행 후로 나뉜다
        Object response = joinPoint.proceed();

        log.info("ADMIN API RESPONSE: userId={}, url={}, response={}",
                userId, requestURI, toJson(response));

        return response;
    }

    private Object extractRequestBody(ProceedingJoinPoint joinPoint){
        // 현재 실행 중인 메서드의 시그니처(메서드명, 파라미터 정보)를 가져옴
        // AOP에서는 MethodSignature로 캐스팅해야 파라미터 정보에 접근 가능
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 컨트롤러 메서드의 실제 인자 값들을 배열로 가져옴
        Object[] arg = joinPoint.getArgs();

        // 메서드의 각 파라미터별 어노테이션 정보를 2차원 배열로 가져옴
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();

        // 각 파라미터를 돌면서 @RestController가 붙었는지 검증
        // 그 파라미터의 실제 값 arg[i]를 리턴
        for (int i = 0; i < parameterAnnotations.length; i++){
            for(Annotation annotation : parameterAnnotations[i]){
                if(annotation.annotationType() == RequestBody.class){
                    return arg[i];
                }
            }
        }
        return null;
    }

    private String toJson(Object obj){
        try{
            // obj를 Json 형태의 문자열로 변환하는 메서드
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex){
            return "could not serialize object";
        }
    }


}
