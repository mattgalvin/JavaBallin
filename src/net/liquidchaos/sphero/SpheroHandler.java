package net.liquidchaos.sphero;

import java.io.IOException;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpheroHandler extends Observable implements Runnable {

    private Thread thread;
    private Sphero sphero;
    private String spheroAddress;
    private static SpheroHandler handler;
    
	final Logger logger = LoggerFactory.getLogger(SpheroHandler.class);

	private SpheroHandler() {
		
	}
	
	public static SpheroHandler getHandler(String spheroAddress) {
		handler = new SpheroHandler();
		handler.spheroAddress = spheroAddress;
		return handler;
	}
	
	@Override
	public void run() {
		try {
			logger.debug("Connection to Sphero at " + spheroAddress);
			sphero = new Sphero(spheroAddress);
		} catch (IOException ioe) {
			logger.error("Could not connect to Sphero.", ioe);
		}
	}

    public void connect() throws Exception {
    	
    	if (thread != null) {
    		throw new Exception("A SpheroHandler is already connected.");
    	}
    	
        thread = new Thread(this);
        thread.setName("SpheroHandler");
        thread.start();
        logger.trace("Started SpheroHandler thread.");
    }

    public void disconnect() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
        logger.trace("Stopped SpheroHandler thread.");
    }

}
