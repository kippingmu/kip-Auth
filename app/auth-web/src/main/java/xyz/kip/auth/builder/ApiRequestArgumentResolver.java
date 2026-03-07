package xyz.kip.auth.builder;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.kip.open.common.dto.ApiRequest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author xiaoshichuan
 * @version 2026-03-05 19:28
 */
public class ApiRequestArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 只处理 ApiRequest 参数，并且不处理带 @RequestBody 的（POST/PUT/PATCH 交给 RequestBodyAdvice）
        boolean isApiRequest = ApiRequest.class.isAssignableFrom(parameter.getParameterType());
        boolean isRequestBody = parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class);

        // 推荐：要求参数上标记 @ApiReq，避免误匹配；如果你想更“无感”，可去掉该限制
        boolean hasApiReq = parameter.hasParameterAnnotation(ApiReq.class);

        boolean skip = parameter.hasMethodAnnotation(SkipApiWrap.class)
                || parameter.hasParameterAnnotation(SkipApiWrap.class);

        return isApiRequest && !isRequestBody && hasApiReq && !skip;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest httpReq = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        Class<?> bodyClass = resolveBodyClass(parameter);
        Object body = bodyClass.getDeclaredConstructor().newInstance();

        // 把 query 参数绑定到 body DTO
        WebDataBinder binder = binderFactory.createBinder(webRequest, body, "body");
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        binder.bind(new org.springframework.beans.MutablePropertyValues(servletRequest.getParameterMap()));

        if (binder.getBindingResult().hasErrors()) {
            throw new BindException(binder.getBindingResult());
        }

        ApiRequest<Object> wrapped = new ApiRequest<>();
        wrapped.setHeader(ApiHeaderBuilder.build(httpReq));
        wrapped.setData(body);
        return wrapped;
    }

    private Class<?> resolveBodyClass(MethodParameter parameter) {
        // ApiRequest<T> 的 T
        Type generic = parameter.getGenericParameterType();
        if (generic instanceof ParameterizedType pt) {
            Type t = pt.getActualTypeArguments()[0];
            if (t instanceof Class<?> c) {
                return c;
            }
        }
        // 兜底：没有泛型就当 Object（但建议每个接口都写明确泛型）
        return Object.class;
    }
}
