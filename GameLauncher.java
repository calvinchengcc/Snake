import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameLauncher {
	
	public static void main(String[] args) {
		
		Snake game = new Snake(); // define applet of interest
	    Frame myFrame = new Frame("Snake"); // create frame with title
	    myFrame.add(game);
	    
	    //myFrame.pack(); // set window to appropriate size (for its elements)
	    myFrame.setLocation(100, 100);
	    myFrame.setResizable(false);
	    myFrame.pack();
	    myFrame.setIconImage(game.getIconImage());
	    myFrame.setVisible(true); // usual step to make frame visible
	    
	    myFrame.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent we) {
	    		System.exit(0);
	    	}
	    });
	    
	    game.requestFocus();
	    game.init();
	}
}
