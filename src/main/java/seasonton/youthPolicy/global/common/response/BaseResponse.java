package seasonton.youthPolicy.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "statusCode", "message", "result"})
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final boolean isSuccess;

    private final String statusCode;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public static <T> BaseResponse<T> onSuccess(SuccessStatus status, T result) {
        return new BaseResponse<>(
                true,
                status.getCode(),
                status.getMessage(),
                result);
    }

    public static <T> BaseResponse<T> onFailure(ErrorStatus status, T result) {
        return new BaseResponse<>(
                false,
                status.getCode(),
                status.getMessage(),
                result);
    }
}
