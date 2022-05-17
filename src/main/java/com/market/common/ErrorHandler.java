package com.market.common;

import com.market.exception.MarketException;

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
    public @ResponseBody com.market.common.ErrorResponse handleException(Exception e) {
        logger.error(Constants.ERROR_LOG_PREFIX, e);

        if (e instanceof MarketException) {
            return new com.market.common.ErrorResponse(e.getMessage());
        }

        return new com.market.common.ErrorResponse(Constants.GENERIC_ERROR_MSG);
    }
}
