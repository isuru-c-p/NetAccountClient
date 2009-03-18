import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class NetLoginApplet extends JApplet
       implements ActionListener
{
	private NetLoginGUI nlgui;
	
	public NetLoginApplet(){
		init();
	}

	public void init () {
		try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // handle exception
        } 
	  nlgui=new NetLoginGUI();
	}
	
	/** Close the frame. **/
	void close () {
	    nlgui.dispose ();
	    nlgui = null;
	  } // close
	
	public void actionPerformed (ActionEvent e) {
	    String command = e.getActionCommand ();
	    if (command.equals ("Close")) {
	        close ();
	    } else { // Open
	        if (nlgui == null) {
	        	nlgui = new NetLoginGUI();
	        	nlgui.setVisible (true);
	        }
	    }
	  } // actionPerformed
	
}