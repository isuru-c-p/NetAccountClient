package nz.ac.auckland.netlogin.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Icons {

	private static Icons instance;

	private static final int[] windowIconSizes = { 16, 32, 128, 256 };
	private java.util.List<Image> windowIcons;
    private Image iconDefault;
    private Image iconConnected;
    private Image iconConnecting;
    private Image iconDisconnected;
	private Image spinner;

	public static Icons getInstance() {
		if (instance == null) instance = new Icons();
		return instance;
	}

	private Icons() {
		loadImages();
	}

	private void loadImages() {
        iconDefault = loadImage("StatusIcon.png");
        iconConnected = loadImage("StatusIconConnected.png");
        iconConnecting = loadImage("StatusIconConnecting.png");
        iconDisconnected = loadImage("StatusIconDisconnected.png");
		spinner = loadImage("Spinner.png");

		windowIcons = new ArrayList<Image>();
		for(int iconSize : windowIconSizes) {
			windowIcons.add(loadImage("AppIcon " + iconSize + ".png"));
		}
	}

	private Image loadImage(String imageName) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.getImage(this.getClass().getResource("/" + imageName));
	}

	public Image getConnectedIcon() {
		return iconConnected;
	}

	public Image getConnectingIcon() {
		return iconConnecting;
	}

	public Image getDefaultIcon() {
		return iconDefault;
	}

	public Image getDisconnectedIcon() {
		return iconDisconnected;
	}

	public List<Image> getWindowIcons() {
		return windowIcons;
	}

	public Image getSpinner() {
		return spinner;
	}
}
