package seasonton.youthPolicy.domain.report.exception;

import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.exception.GeneralException;

public class ReportException extends GeneralException {
    public ReportException(ErrorStatus code) {
        super(code);
    }
}
