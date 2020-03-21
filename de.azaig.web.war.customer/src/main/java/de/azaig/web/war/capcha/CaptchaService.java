package de.azaig.web.war.capcha;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService extends SecureCodeStore {

	private static final char[] ALLOWED = "abcdefghjkmnpqrtuvwxyzABCDEFGHJKLMNPQRTUVWXY34567".toCharArray();

	@Inject
	private final SecureCodeStore scs = null;

	public String generateCaptcha(long javascriptValue, HttpServletRequest req) throws NoSuchAlgorithmException {
		long code = javascriptValue;
		int hashCode = req.getRemoteAddr().hashCode();
		long secure = scs.getSecure();
		MessageDigest md5 = MessageDigest.getInstance("md5");
		String plain = code + "-" + hashCode + "-" + secure;
		byte[] digest = md5.digest(plain.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : digest) {
			int bk = Math.abs(b % ALLOWED.length);
			sb.append(ALLOWED[bk]);
			if (sb.length() > 3) {
				break;
			}
		}
		return sb.toString();
	}

}
