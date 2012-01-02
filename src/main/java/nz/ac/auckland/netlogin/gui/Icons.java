package nz.ac.auckland.netlogin.gui;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Icons {

	private static Icons instance;

	private static final int[] windowIconSizes = { 16, 32, 128, 256 };
	private ArrayList<Image> windowIcons;

	public static Icons getInstance() {
		if (instance == null) instance = new Icons();
		return instance;
	}

	private Icons() {
		loadImages();
	}

	private void loadImages() {
		windowIcons = new ArrayList<Image>();
		for(int iconSize : windowIconSizes) {
			Image image = loadImage("AppIcon " + iconSize + ".png");
			if (image != null) windowIcons.add(image);
		}
	}

	private Image loadImage(String imageName) {
		Image image = null;
		
		InputStream stream = this.getClass().getResourceAsStream("/" + imageName);
		if (stream != null) {
			try {
				image = new Image(Display.getDefault(), stream);
			} catch (SWTException e) {
				// treat it the same as image not found
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		if (image == null) throw new RuntimeException("Image not found: " + imageName);
		return image;
	}

	public Image[] getWindowIcons() {
		return windowIcons.toArray(new Image[windowIcons.size()]);
	}

	public Image getLargestIcon() {
		return windowIcons.get(windowIcons.size() - 1);
	}

	public Image getClosestIcon(int size) {
		if (windowIcons.isEmpty()) return null;
		for(Image icon : windowIcons) {
			if (icon.getBounds().width >= size) return icon;
		}
		return windowIcons.get(windowIcons.size() - 1);
	}

}
