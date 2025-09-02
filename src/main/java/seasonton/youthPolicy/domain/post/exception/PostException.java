package seasonton.youthPolicy.domain.post.exception;

import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.exception.GeneralException;

public class PostException extends GeneralException {
    public PostException(ErrorStatus code) {
        super(code);
    }
}
