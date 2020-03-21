package de.azaig.web.war.capcha;

import java.security.SecureRandom;

public class SecureCodeStore {
	private final static SecureRandom sr = new SecureRandom();

	private long regenerateTimestamp = 0;

	private Long secure;

	public long getSecure() {
		if (secure == null || regenerateTimestamp < System.currentTimeMillis()) {
			secure = sr.nextLong();
			regenerateTimestamp = System.currentTimeMillis() + 1000 * 60 * 5;
		}
		return secure;
	}
}
