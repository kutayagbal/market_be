package com.market.common;

public class ErrorResponse {
	private final String errorMessage;

	public ErrorResponse(String errMsg) {
		errorMessage = errMsg;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}
