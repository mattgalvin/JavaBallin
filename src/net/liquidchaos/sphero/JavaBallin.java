package net.liquidchaos.sphero;

import java.awt.Color;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import net.liquidchaos.controller.ControllerHandler;
import net.liquidchaos.controller.XBoxControllerData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaBallin implements Observer {
	
	final Logger logger = LoggerFactory.getLogger("JavaBallin");

	private Display display;
	private Shell shell;
	private Label spheroName;
	private Label spheroSpeed;
	private Label spheroHeading;
	private XBoxControllerData controllerData;
	private int spheroState = 0; // 0 - nothing, 1 - set heading, 2 - roll
	private int speed;
	private int heading;
	
	private Sphero sphero;
	private ControllerHandler controller;

	public JavaBallin() {
		 controller = ControllerHandler.getHandler();
		 controller.addObserver(this);
	}

	public void run() {
		display = new Display();
		shell = new Shell(display);
		shell.setText("JavaBallin'");
		shell.setSize(500, 300);

		shell.setLayout(new GridLayout());

		// Set up information panel
		Group infoPanel = new Group(shell, SWT.SHADOW_ETCHED_IN);
		infoPanel.setText("Sphero Information");

		GridData infoGD = new GridData(GridData.FILL, GridData.FILL, true, true);
		infoPanel.setLayoutData(infoGD);
		
		infoPanel.setLayout(new GridLayout(2, false));
		
		Label spheroNameLabel = new Label(infoPanel, SWT.NONE);
		spheroNameLabel.setText("Sphero Name:");

		spheroName = new Label(infoPanel, SWT.NONE);
		spheroName.setText("...");
		
		Label spheroSpeedLabel = new Label(infoPanel, SWT.NONE);
		spheroSpeedLabel.setText("Sphero Speed:");
		spheroSpeed = new Label(infoPanel, SWT.NONE);

		Label spheroHeadingLabel = new Label(infoPanel, SWT.NONE);
		spheroHeadingLabel.setText("Sphero Heading:");
		spheroHeading = new Label(infoPanel, SWT.NONE);

		// Set up buttons
		Composite buttonHolder = new Composite(shell, SWT.NONE);
		buttonHolder.setLayout(new GridLayout());

		Button button = new Button(buttonHolder, SWT.PUSH);
		button.setText("Connect");

		button.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	try {
		    		sphero = new Sphero("6886E7014B22");
		    		spheroName.setText(sphero.getBluetoothInfo().getName());
		    		spheroName.setSize(50, spheroName.getSize().y);
		    		setSpheroState();
		    	} catch (IOException ioe) {
		    		
		    	}
		    }
		}); 

		Composite radioHolder = new Composite(buttonHolder, SWT.NONE);
		radioHolder.setLayout(new RowLayout());
		
		Button[] radioButtons = new Button[3];
		radioButtons[0] = new Button(radioHolder, SWT.RADIO);
		radioButtons[0].setSelection(true);
		radioButtons[0].setText("Nothing");
		radioButtons[0].addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	spheroState = 0;
		    	setSpheroState();
		    }
		}); 
	 
		radioButtons[1] = new Button(radioHolder, SWT.RADIO);
		radioButtons[1].setText("Set Heading");
		radioButtons[1].addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	spheroState = 1;
		    	setSpheroState();
		    }
		}); 
	 
		radioButtons[2] = new Button(radioHolder, SWT.RADIO);
		radioButtons[2].setText("Let's Roll!");
		radioButtons[2].addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	spheroState = 2;
		    	setSpheroState();
		    }
		}); 
		
		// Center the dialog
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		try {
			controller.start();
		} catch (Exception e) {
			logger.error("Could not start controller.");
		}
		
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		if (sphero != null) {
			try {
				sphero.setBackLEDOutput((byte)0);
	    		sphero.setRoll((byte)0, (short)0, (byte)1);
			} catch (IOException ioe) {
				logger.error("Could not shut down Sphero properly.", ioe);
			}
		}
		
		controller.stop();
	}
	
	private void setSpheroState() {
		try {
			switch (spheroState) {
			case 1 :
				sphero.setBackLEDOutput((byte)255);
				break;
			default :
				sphero.setBackLEDOutput((byte)0);
				break;
			}
		} catch (IOException ioe) {
			// nothing
		}
	}

	public void update(Observable o, Object arg) {
		if (o instanceof ControllerHandler) {
			controllerData = (XBoxControllerData)arg;

	    	float rawSpeed = controllerData.getLeftRadius();
	    	rawSpeed = rawSpeed < 0.2f ? 0.0f : rawSpeed;
	    	rawSpeed = rawSpeed > 1.0f ? 1.0f : rawSpeed;
	    	
	    	speed = (int)(255.0f * rawSpeed);
	    	
	    	try {
	    		if (sphero != null) {
	    			boolean setColor = true;
	    			
		    		switch (spheroState) {
		    		case 1 :
		    			if (speed > 0) {
		    		    	heading = 360 - controllerData.getLeftAngle();
		    		    	heading = heading < 180 ? heading + 180 : heading - 180;
		    				sphero.setHeading((short)heading);
	//	    				sphero.setRoll((byte)0, (short)heading, (byte)0);
		    				sphero.setRoll((byte)0, (short)heading, (byte)1);
		    			}
			    		break;
		    		case 2 :
		    			if (controllerData.isRightButton()) {
			    			sphero.setLEDColor((byte)255, (byte)0, (byte)0, false);
		    				sphero.setRoll((byte)0, (short)heading, (byte)0);
		    				setColor = false;
		    			} else {
			    			if (speed == 0) {
			    				sphero.setRoll((byte)0, (short)heading, (byte)0);
			    			} else {
			    		    	heading = 360 - controllerData.getLeftAngle();
			    		    	heading = heading < 180 ? heading + 180 : heading - 180;
			    				sphero.setRoll((byte)speed, (short)heading, (byte)1);
			    			}
		    			}
			    		break;
			    	default :
			    		break;
		    		}
		    		
		    		if (setColor) {
			    		float h = controllerData.getRightAngle()/360.0f;
			    		float s = controllerData.getRightRadius() > 1.0f ? 1.0f : controllerData.getRightRadius();
			    		float v = 1.0f;
		    		
		    			Color c = Color.getHSBColor(h,s,v);
		    			sphero.setLEDColor((byte)c.getRed(), (byte)c.getGreen(), (byte)c.getBlue(), false);
		    		}
	    		}
	    	} catch (IOException ioe) {
	    		
	    	}

			Display.getDefault().asyncExec(new Runnable() {
			    public void run() {
			    	spheroSpeed.setText(String.format("%d", speed));
			    	spheroHeading.setText(String.format("%d", heading));
			    	
			    	spheroSpeed.setSize(50, spheroName.getSize().y);
			    	spheroHeading.setSize(50, spheroName.getSize().y);
			    }
			});
		}
	}
	
	public static void main(String[] args) {
		JavaBallin jb = new JavaBallin();
		jb.run();
	}

}
