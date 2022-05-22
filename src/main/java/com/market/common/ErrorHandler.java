package com.market.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    private final Log logger = LogFactory.getLog(ErrorHandler.class);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleException(Exception e) {
        logger.error(Constants.ERROR_LOG_PREFIX, e);

        if (e instanceof MarketException) {
            return new ErrorResponse(e.getMessage());
        }

        return new ErrorResponse(Constants.GENERIC_ERROR_MSG);
    }

    private class ErrorResponse {
        private final String errorMessage;

        public ErrorResponse(String errMsg) {
            errorMessage = errMsg;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }
}
