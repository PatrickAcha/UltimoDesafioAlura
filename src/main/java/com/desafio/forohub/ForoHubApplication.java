package com.desafio.forohub;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SpringBootApplication
public class ForoHubApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ForoHubApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException {
		// Mensaje del gato ASCII
		System.out.println("/\\_/\\");
		System.out.println("( o.o )");
		System.out.println("> ^ <");
		System.out.println(("------"));
		System.out.println("¡Bienvenido a ForoHub! Gracias por usar nuestro servicio.");

		// Arte ASCII
		int width = 200;
		int height = 60;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setFont(new Font("SansSerif", Font.BOLD, 48));

		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.drawString("JAVA", 20, 50);

		for (int y = 0; y < height; y++) {
			StringBuilder sb = new StringBuilder();
			for (int x = 0; x < width; x++) {
				sb.append(image.getRGB(x, y) == -16777216 ? " " : "$");
			}

			if (sb.toString().trim().isEmpty()) {
				continue;
			}

			System.out.println(sb);
		}
	}
}