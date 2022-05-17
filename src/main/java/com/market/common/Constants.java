package com.market.common;

public class Constants {
	public static final String ERROR_LOG_PREFIX = "MARKET_ERROR";

	public static final short ORDER_LIST_PAGE_SIZE = 100;
	public static final short ORDER_LIST_PAGE_NUMBER_MAX_SIZE = 1000;

	public static final String GENERIC_ERROR_MSG = "An error occured. Try agin later!";
	public static final String NO_USR_MSG = "No user found!";
	public static final String NO_STOCK_MSG = "No stock found!";
	public static final String INV_PAGE_MSG = "Invalid page number!";
	public static final String INV_QUANTITY_MSG = "Quantity can not be negative or zero!";
	public static final String NOT_ENOUGH_SUPPLY = "There is not enough stock for this supply!";
	public static final String NOT_ENOUGH_BALANCE = "There is not enough balance for this demand!";
	public static final Object NOT_ENOUGH_BALANCE_LOG = "There is not enough balance for this demand. It will be removed!";
	public static final Object NOT_ENOUGH_STOCK_LOG = "There is not enough stock for this supply. It will be removed!";

	public enum Roles {
		SCOPE_TRADER, SCOPE_ADMIN
	}
}
