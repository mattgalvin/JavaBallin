package net.liquidchaos.controller;

import java.util.Observable;

import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerHandler extends Observable implements Runnable  {

    private Thread thread;
    private static ControllerHandler handler;
    
	final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);

	private ControllerHandler() {
		// TODO Auto-generated constructor stub
	}

	public static ControllerHandler getHandler() {
		handler = new ControllerHandler();
		return handler;
	}

	@Override
	public void run() {
		try {
			while (thread != null) {
				Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
				Controller controller = null;
				
				for (Controller c : controllers) {
					if (c.getName().equals("Microsoft X-Box 360 pad")) {
						controller = c;
					}
				}
				
				if (controller == null) {
					logger.error("No Microsoft X-Box 360 pad controllers found.");
					stop();
				}
				
				controller.poll();
				
				float x = controller.getComponent(Identifier.Axis.X).getPollData();
				float y = controller.getComponent(Identifier.Axis.Y).getPollData();
				float rx = controller.getComponent(Identifier.Axis.RX).getPollData();
				float ry = controller.getComponent(Identifier.Axis.RY).getPollData();
				
				XBoxControllerData data = new XBoxControllerData(x, y, rx, ry);
				logger.trace(String.format("Left (%.2f, %d)\tRight (%.2f, %d)", data.getLeftRadius(), data.getLeftAngle(), data.getRightRadius(), data.getRightAngle()));

				setChanged();
				notifyObservers(data);
	
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// Nothing
		}
	}

    public void start() throws Exception {
    	if (thread == null) {
	        thread = new Thread(this);
	        thread.setName("ControllerHandler");
	        thread.start();
	        logger.trace("Started ControllerHandler thread.");
    	} else {
    		logger.warn("An ControllerHandler is already running.");
    	}
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
        logger.trace("Stopped ControllerHandler thread.");
    }
}
