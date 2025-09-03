package seasonton.youthPolicy.domain.policy.exception;

import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.exception.GeneralException;

public class PolicyException extends GeneralException {
    public PolicyException(ErrorStatus code) {
        super(code);
    }
}
