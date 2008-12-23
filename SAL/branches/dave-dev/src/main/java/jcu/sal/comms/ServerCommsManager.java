
package jcu.sal.comms;

import jcu.sal.comms.listeners.CommandProcessor;
import jcu.sal.comms.listeners.TransportCommandListener;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.comms.transport.ServerTransport;
import jcu.sal.xml.Command;
import jcu.sal.xml.Response;
import jcu.sal.xml.TransportCommand;
import jcu.sal.xml.TransportResponse;

public class ServerCommsManager implements TransportCommandListener {

	private ServerTransport transport = null;
	private CommandProcessor processor = null;

	public ServerCommsManager() {
	}

	public void setTransport(ServerTransport transport) {
		this.transport = transport;
	}

	public ServerTransport getTransport() {
		return transport;
	}

	public void setProcessor(CommandProcessor processor) {
		this.processor = processor;
	}

	public CommandProcessor getProcessor() {
		return processor;
	}

	public void setup() {
		transport.setup();
		transport.setCommandListener(this);
	}

	public void shutdown() {
		transport.shutdown();
	}

	private void send(int command_id, Response r) {
		TransportResponse tr = new TransportResponse();
		tr.setId(command_id);
		tr.setResponse(r);

		transport.send(tr);
	}

	public void receivedCommand(TransportCommand tc) {
		processor.process(tc.getCommand(), new ProcessingResponseListener(this, tc.getId()));
	}

	private class ProcessingResponseListener implements ResponseListener {

		public ServerCommsManager manager;
		public int command_id;

		public ProcessingResponseListener(ServerCommsManager manager, int command_id) {
			this.manager = manager;
			this.command_id = command_id;
		}

		public void receivedResponse(Response r) {
			manager.send(command_id, r);
		}
	}
}