package jcu.sal.events;

import javax.naming.ConfigurationException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestEventDispatcher extends TestCase implements EventHandler {
	
	private class TestEvent extends Event {

		public TestEvent(int t, String sid, String p) {
			super(t, sid, p);
		}

		public TestEvent(int t, String sid, String doc, String p) {
			super(t, sid, doc, p);
		}

	}
	
	int i;
	int type;
	static int NB_EVENTS = 500;
	EventDispatcher ev;
	String producer = "TestEventDispatcher";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		i=0;
		type=0;
		ev = EventDispatcher.getInstance();
		
	}

	@After
	public void tearDown() throws Exception {
		ev.stop();
		super.tearDown();
	}
	
	@Test
	public void testAddRemoveProducer() {
		assertTrue(ev.addProducer(producer));
		assertTrue(ev.removeProducer(producer));
		assertTrue(ev.addProducer(producer));
		assertFalse(ev.addProducer(producer));
		assertFalse(ev.addProducer(producer));
		assertTrue(ev.removeProducer(producer));
	}
	
	@Test
	public void testRegisterUnregisterEventHandler() {
		try {
			assertTrue(ev.addProducer(producer));
			ev.registerEventHandler(this, producer);
			ev.unregisterEventHandler(this, producer);
			assertFalse(ev.addProducer(producer));
			assertTrue(ev.removeProducer(producer));
		} catch (ConfigurationException e) {
			fail("Got exception");
		}
		
		try {
			ev.registerEventHandler(this, producer);
			fail("Didnt get exception");
		} catch (ConfigurationException e) {}
		
		try {
			assertTrue(ev.addProducer(producer));
			ev.registerEventHandler(this, producer);
			ev.unregisterEventHandler(this, producer);
		} catch (ConfigurationException e) {fail("Got exception");}
		
		try {
			ev.unregisterEventHandler(this, producer);
			fail("Didnt get exception");
		} catch (ConfigurationException e) {}
	}

	@Test
	public void testQueueEvent() {
		try {
			assertTrue(ev.addProducer(producer));
			ev.registerEventHandler(this, producer);
		} catch (ConfigurationException e) {
			fail("Got exception");
		}
		while(type++ < NB_EVENTS){
			try {
				System.out.println("("+System.currentTimeMillis() +") queueing event "+type);  
				ev.queueEvent(new TestEvent(type, "SourceID", producer));
			} catch (ConfigurationException e) {
				fail("Couldnt queue event "+type);
			}
		}
		//Sleep for 10 seconds to allow events to be dispatched
		try {Thread.sleep(1*1000);}
		catch (InterruptedException e) {e.printStackTrace();}
		
		assertTrue(i==NB_EVENTS);
	}

	public String getName() {
		return "TestEventDisptacher";
	}

	public void handle(Event e) {
		System.out.println("("+System.currentTimeMillis()+") Received event "+i+" from producer "+e.getProducer()+ ", source ID:"+e.getSourceID());
		i++;
	}

}
