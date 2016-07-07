package com.lukekorth.fivehundredpx;

public class FiveHundredException extends Exception {

	private int statusCode = 0;
	
	public FiveHundredException() {
		super();
	}

	public FiveHundredException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public FiveHundredException(String detailMessage) {
		super(detailMessage);
	}

	public FiveHundredException(Throwable throwable) {
		super(throwable);
	}
	
	public FiveHundredException(int status) {
		super();
		this.statusCode = status;
	}

    public int getStatusCode() {
        return statusCode;
    }
}
