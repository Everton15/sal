package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jcu.sal.common.Slog;

import org.apache.log4j.Logger;

/**
 * This class implements a thread that blocks until {@link QueueElement} objects
 * have been queued for delivery. When one is available, the thread dispatches
 * it.
 * @author gilles
 *
 */
public class StreamDataDispatcher implements Runnable{
	private static Logger logger = Logger.getLogger(StreamDataDispatcher.class);
	static {Slog.setupLogger(logger);}
	
	private static Thread t;
	private static BlockingQueue<QueueElement> dataQueue;
	private final static StreamDataDispatcher dispatcher = 
		new StreamDataDispatcher();
	
	private StreamDataDispatcher(){
		dataQueue = null;
		t = null;
	}
	
	/**
	 * This method starts the dispatcher thread if it is not already running.
	 * If it is this method does nothing
	 */
	public synchronized static void start(){
		if(t==null){
			dataQueue = new LinkedBlockingQueue<QueueElement>();
			t = new Thread(dispatcher, "SALSpot stream data dispatcher");
			t.start();
		}
	}
	
	/**
	 * This method stops the dispatcher thread if it is running. If it is not
	 * this method does nothing.
	 */
	public synchronized static void stop(){
		if(t!=null && t.isAlive()){
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			t = null;
			dataQueue.clear();
			dataQueue = null;
		}
	}
	
	/**
	 * This method queues a {@link QueueElement} object for delivery.
	 * If {@link #start()} has not been called prior to calling this method,
	 * the {@link QueueElement} will not be delivered, and 
	 * {@link QueueElement#stopStream()} will be called to stop the stream.
	 * @param d the {@link QueueElement} object to be delivered
	 */
	public static void queueData(QueueElement d){
		if(t!=null){
			if(!dataQueue.offer(d))
				logger.error("StreamDataDispatcher queue full");
		} else {
			logger.error("Queuing data in StreamDataDispatcher but "+
					"thread not started - stopping stream");
			try {
				logger.error("Queued element:\n"+d.data.getData());
			} catch (StreamClosedException e) {
				logger.error("Queued element:\nStreamClosedException "+
						e.getMessage());
			} catch (StreamException e) {
				logger.error("Queued element:\nStreamException"+
						e.getMessage());
			}
			d.stopStream();
		}
	}
	
	public void run(){
		try {
			while(!Thread.interrupted())
				dataQueue.take().deliver();
		}catch (InterruptedException e){}
	}
	
	/**
	 * This class encapsulates a single data element from a stream of data from
	 * a sal spot, and the recipient of this data element.
	 * This class can be used to deliver the data element to the recipient, 
	 * by calling the {@link #deliver()} method. If upon delivery, the 
	 * recipient throws an exception, this class can notify the SAL spot to
	 * stop the stream, if the 
	 * {@link QueueElement#QueueElement(StreamData, StreamDataHandler, SpotCommandSender, String, String)
	 * is used.
	 * @author gilles
	 *
	 */
	public static class QueueElement{
		private StreamData data;
		private StreamDataHandler handler;
		private SpotCommandSender errorHandler;
		private String sensor, cmd;
		
		/**
		 * This method builds a new {@link QueueElement} object with the given
		 * data element & recipient. If upon delivery of the data, the recipient
		 * (the handler) throws an exception, the given 
		 * {@link SpotCommandSender} will be used to instruct the SAL spot to
		 * stop the data stream from the sensor <code>s</code> & command 
		 * <code>c</code>. 
		 * @param d the data element
		 * @param h the recipient of the data element
		 * @param e the command sender object which will be used to notify a 
		 * SAL spot to stop the stream if the handler throws an exception while
		 * being handed in the data stream
		 * @param s the sensor which produces the stream
		 * @param c the command used to start the stream
		 * @throws RuntimeException if e is not null but either c or s is.
		 */
		public QueueElement(StreamData d, StreamDataHandler h, 
				SpotCommandSender e, String s, String c){
			if(e!=null && (s==null || c==null))
				throw new RuntimeException("Error in arguments: e:"+
						e+" s:"+" c:"+c);
			data = d;
			handler = h;
			errorHandler = e;
			sensor = s;
			cmd = c;
		}
		
		/**
		 * This method builds a new {@link QueueElement} object with the given
		 * data element & recipient. If upon delivery of the data, the recipient
		 * (the handler) throws an exception, this information will be lost.
		 * @param d the data element
		 * @param h the recipient of the data element
		 */
		public QueueElement(StreamData d, StreamDataHandler h){
			this(d,h,null,null,null);
		}
		
		/**
		 * this method invokes the data handler and hands
		 * it the {@link StreamData} object.
		 */
		public void deliver(){
			try {
				handler.handle(data);
			} catch (Throwable t) {
				System.out.println("Error delivering stream data"
						+" - stopping stream");
				
					stopStream();
			}
		}
		
		/**
		 * This method sends a STOP stream command to stop the stream
		 */
		private void stopStream(){
			if(errorHandler!=null) {
				try {
					errorHandler.sendStopCommand(sensor, cmd);
				} catch (IOException e) {
					//bad luck, error sending the command...
					System.out.println("ERROR SENDING STOP COMMAND");
					e.printStackTrace();
				}
			}
		}
	}
}
