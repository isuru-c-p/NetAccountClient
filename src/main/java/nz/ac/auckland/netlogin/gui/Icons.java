package nz.ac.auckland.netlogin.gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Icons {

	private static Icons instance;

	private static final int[] windowIconSizes = { 16, 32, 128, 256 };
	private List<BufferedImage> windowIcons;

	public static Icons getInstance() {
		if (instance == null) instance = new Icons();
		return instance;
	}

	private Icons() {
		loadImages();
	}

	private void loadImages() {
		windowIcons = new ArrayList<BufferedImage>();
		for(int iconSize : windowIconSizes) {
			windowIcons.add(loadImage("AppIcon " + iconSize + ".png"));
		}
	}

	private BufferedImage loadImage(String imageName) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(this.getClass().getResource("/" + imageName));
		} catch (IOException e) {
			// treat it the same as image not found
		}
		if (image == null) throw new RuntimeException("Image not found: " + imageName);
		return image;
	}

	public List<? extends Image> getWindowIcons() {
		return windowIcons;
	}

	public Image getClosestIcon(Dimension size) {
		for(BufferedImage icon : windowIcons) {
			if (icon.getWidth() >= size.getWidth()) return icon;
		}
		return windowIcons.get(windowIcons.size() - 1);
	}

}
