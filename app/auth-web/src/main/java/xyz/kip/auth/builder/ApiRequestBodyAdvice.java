package xyz.kip.auth.builder;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import xyz.kip.open.common.dto.ApiRequest;

import java.lang.reflect.Type;

/**
 * @author xiaoshichuan
 * @version 2026-03-05 19:21
 */
public class ApiRequestBodyAdvice extends RequestBodyAdviceAdapter {
    @Override
    public boolean supports(MethodParameter parameter,
                            Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {

        // 只拦截：参数类型是 ApiRequest 且没有跳过注解
        return ApiRequest.class.isAssignableFrom(parameter.getParameterType())
                && !parameter.hasMethodAnnotation(SkipApiWrap.class)
                && !parameter.hasParameterAnnotation(SkipApiWrap.class);
    }

    @Override
    public Object afterBodyRead(Object body,
                                HttpInputMessage inputMessage,
                                MethodParameter parameter,
                                Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {

        // 非 JSON（multipart/form-data / octet-stream）直接放行
        MediaType ct = inputMessage.getHeaders().getContentType();
        if (ct == null || !MediaType.APPLICATION_JSON.isCompatibleWith(ct)) {
            return body;
        }

        // 如果客户端本来就传了 ApiRequest，原样返回
        if (body instanceof ApiRequest) {
            // 也可以在这里补全 header.ts 等字段（按需要）
            return body;
        }

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        ApiRequest<Object> wrapped = new ApiRequest<>();
        wrapped.setHeader(ApiHeaderBuilder.build(req));
        wrapped.setData(body);
        return wrapped;
    }
}
