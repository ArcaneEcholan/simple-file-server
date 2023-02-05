package fit.wenchao.http_file_server.interceptor;

import fit.wenchao.http_file_server.utils.DateTimeUtils;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("get into LogInterceptor");
        String ipAddress = IPUtils.getIpAddr(request);
        log.info("接口来访ip：" + ipAddress);
        String now = DateTimeUtils.nowString();
        log.info("访问时间：" + now);
        log.info("访问接口：" + request.getRequestURI());
        log.info("访问方式：" + request.getMethod());
        log.info("MIME类型：" + request.getContentType());
        return true;
    }
}
