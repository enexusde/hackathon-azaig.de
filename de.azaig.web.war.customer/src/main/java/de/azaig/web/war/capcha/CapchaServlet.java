package de.azaig.web.war.capcha;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CapchaServlet {

	@Inject
	private final CaptchaService service = null;

	@GetMapping("/capcha.png")
	private ResponseEntity<byte[]> getCatpcha(long javascriptValue, HttpServletRequest req) throws IOException, NoSuchAlgorithmException {
		BufferedImage bi = new BufferedImage(50, 20, BufferedImage.TYPE_INT_RGB);

		Graphics g = bi.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, 50, 20);
		g.setColor(Color.white);
		String code = service.generateCaptcha(javascriptValue, req);
		g.drawString(code, 5, 15);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", baos);
		byte[] byteArray = baos.toByteArray();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(byteArray.length);
		return new ResponseEntity<byte[]>(byteArray, headers, HttpStatus.OK);
	}
}
