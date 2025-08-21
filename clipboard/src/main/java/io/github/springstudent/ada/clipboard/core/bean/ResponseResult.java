package io.github.springstudent.ada.clipboard.core.bean;

/**
 * @author ZhouNing
 * @date 2024/12/31 11:10
 **/
public class ResponseResult<T> {
    private int code;
    private String msg;
    private T result;

    public ResponseResult(int code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public ResponseResult() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    private static final Integer HTTP_OK = 200;


    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(HTTP_OK, "success", null);

    }

    public static <T> ResponseResult<T> success(T result) {
        return new ResponseResult<>(HTTP_OK, "success", result);
    }

    public static <T> ResponseResult<T> fail(Integer code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    public static String buildSuccessResultStr(Object result) {
        if (null != result && result instanceof String) {
            String res = (String) result;
            String replaceAllStr = res.replaceAll("\"", "\\\\\"");
            return "{\"msg\": \"success\",\"code\": " + HTTP_OK + ",\"result\": \"" + replaceAllStr.replaceAll("\"", "\\\"") + "\"}";
        }
        if (null == result) {
            return "{\"msg\": \"success\",\"code\": " + HTTP_OK + ",\"result\":  " + result + "}";
        }
        return "{\"msg\": \"success\",\"code\": " + HTTP_OK + ",\"result\": \"" + result.toString().replaceAll("\"", "\\\"") + "\"}";
    }

}
