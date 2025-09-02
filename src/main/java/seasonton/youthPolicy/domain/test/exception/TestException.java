package seasonton.youthPolicy.domain.test.exception;

import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.exception.GeneralException;

public class TestException extends GeneralException {

    public TestException(ErrorStatus code) {
        super(code);
    }
}
