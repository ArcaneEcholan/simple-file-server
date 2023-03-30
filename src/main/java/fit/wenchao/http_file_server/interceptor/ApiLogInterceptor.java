package fit.wenchao.http_file_server.interceptor;

import fit.wenchao.http_file_server.utils.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class ApiLogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ipAddress = IPUtils.getIpAddr(request);
        log.debug("{} {} {}", request.getMethod(), request.getRequestURI(), ipAddress);
        return true;
    }
}
