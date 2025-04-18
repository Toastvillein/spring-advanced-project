package org.example.expert.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class MyInterceptorHandler implements HandlerInterceptor {

    // 요청 시작 시간 기록
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        request.setAttribute("startTime",System.currentTimeMillis());

        System.out.println("request: " + request.getMethod()
                + " " + request.getRequestURI()
                + " | URL: " + request.getRequestURL());

        return true;
    }

    // 요청 완료 후 실행
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        long startTime = (long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("reponse: " + request.getMethod()
                    + " " + request.getRequestURI()
                    + " | Status: " + response.getStatus()
                    + " | Time: " + duration + "ms");

    }
}
