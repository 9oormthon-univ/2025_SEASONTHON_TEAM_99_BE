package seasonton.youthPolicy.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import seasonton.youthPolicy.global.error.code.status.ErrorReasonDTO;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException{

    private ErrorStatus code;

    public ErrorReasonDTO getErrorReasonHttpStatus() {
        return this.code.getReasonHttpStatus();
    }
}
