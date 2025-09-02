package seasonton.youthPolicy.domain.member.exception;

import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.exception.GeneralException;

public class UserException extends GeneralException {
    public UserException(ErrorStatus code) {
        super(code);
    }
}
