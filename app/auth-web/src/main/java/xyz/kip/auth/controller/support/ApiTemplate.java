package xyz.kip.auth.controller.support;

import xyz.kip.open.common.base.Result;

/**
 * Controller API template.
 *
 * @param <REQ> request type
 * @param <RESP> response type
 */
public abstract class ApiTemplate<REQ, RESP> {

    public final Result<RESP> handle(REQ req) {
        Result<Void> validateResult = doValidate(req);
        if (validateResult == null) {
            return Result.failure("validateResult不能为空");
        }
        if (validateResult.isSuccess()) {
            return execute(req);
        }
        return Result.failure(validateResult.getCode(), validateResult.getMessage());
    }

    protected abstract Result<Void> doValidate(REQ req);

    protected abstract Result<RESP> execute(REQ req);
}
