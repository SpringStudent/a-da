package io.github.springstudent.ada.transport.core.handler;

import io.github.springstudent.ada.transport.core.bean.ResponseResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author ZhouNing
 * @date 2024/12/31 13:19
 **/
@ControllerAdvice(basePackages = "io.github.springstudent.dekstop.server")
public class ResponseBodyHandler implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (null == body && !selectedConverterType.equals(StringHttpMessageConverter.class)) {
			return ResponseResult.success();
		}
		if (selectedConverterType.equals(StringHttpMessageConverter.class)) {
			return ResponseResult.buildSuccessResultStr(body);
		}
		if(body.getClass().equals(ResponseResult.class)||body.getClass().equals(byte[].class)) {
			return body;
		}
		return ResponseResult.success(body);
	}

}
