/*
* Copyright (C) 2009 Gilles Gigan (gilles.gigan@gmail.com)
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public  License as published by the
* Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package jcu.sal.client.gui.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import jcu.sal.common.Constants;
import avahi4j.Address;
import avahi4j.Avahi4JConstants;
import avahi4j.Client;
import avahi4j.IServiceBrowserCallback;
import avahi4j.IServiceResolverCallback;
import avahi4j.ServiceBrowser;
import avahi4j.ServiceResolver;
import avahi4j.Avahi4JConstants.BrowserEvent;
import avahi4j.Avahi4JConstants.Protocol;
import avahi4j.ServiceResolver.ServiceResolverEvent;
import avahi4j.exceptions.Avahi4JException;


public class BonjourList extends JDialog implements ActionListener, IServiceBrowserCallback, IServiceResolverCallback{
	private static final long serialVersionUID = -7902758558432124211L;
	private static BonjourList dialog = null;
    private static String value = "";
    private JList list;
	private Client client;
	private ServiceBrowser browser;
	private Hashtable<String,String> services;
	private List<ServiceResolver> resolvers;
	private JButton connectButton;
	
    /**
     * Show the list of SAL agents discovered through Bonjour
     */
    public static synchronized String showDialog(Frame frame) {    	
    	if (dialog==null)
    		dialog = new BonjourList(frame);
    	  	
    	// try to start the bonjour client
    	if (dialog.startBonjourClient())
    	{    	
    		// bonjour client started, shows modal dialog box
    		dialog.setVisible(true);
    		
    		// stop the client
    		dialog.stopBonjourClient();
    	} else {
    		// show error box
    		JOptionPane.showMessageDialog(frame,
    			    "The Bonjour client could not be started",
    			    "Bonjour error",
    			    JOptionPane.ERROR_MESSAGE);
    		
        	// reset return value
        	value = null;
    	}
    	
        return value;
    }
    
    /**
     * This method starts the Bonjour client if possible
     * @return true if the client was successfully started
     */
    private boolean startBonjourClient() {
		try {
			client = new Client();
			client.start();
		      
	        // create a service resolver
			browser = client.createServiceBrowser(this, 
					Avahi4JConstants.AnyInterface, Protocol.ANY, 
					Constants.SAL_SERVICE_TYPE, null, 0);
		} catch (Throwable t) {
			// Error starting bonjour client
			System.out.println("Error starting the Bonjour client: "+t.getMessage());
			if(client!=null)
				client.release();
			client = null;
		}

		return client!=null;
    }
    
    /**
     * This method should be called only if {@link #startBonjourClient()} returned
     * <code>true</code>, and will stop the Bonjour client.
     */
    private void stopBonjourClient() {
		// release the browser first so no more ServiceResolver can be added
		// to the list
		browser.release();
		
		// we can now safely release items in the list
		for(ServiceResolver s: resolvers)
			s.release();
		
		// stop and release the client
		client.stop();
		client.release();
    }

    /**
     * This method create a Modal dialog box with a JList where SAL agents will be
     * displayed
     * @param frame the parent frame in the centre of which the dialog box
     * should be displayed
     */
    private BonjourList(Frame frame) {
        super(frame, "SAL Agents", true);
        
        services = new Hashtable<String, String>();
        resolvers = new ArrayList<ServiceResolver>();
  
        //Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        //
        connectButton = new JButton("Connect");
        connectButton.setActionCommand("Connect");
        connectButton.addActionListener(this);
        
        // enable button only when we have some agents to connect to
        connectButton.setEnabled(false);
        getRootPane().setDefaultButton(connectButton);

        // create list
        list = new JList(new String[] {}) {
			private static final long serialVersionUID = 3252319659618389592L;

			public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    connectButton.doClick(); //emulate button click
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        // create list container
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Select a SAL agent to connect to:");
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // add buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(connectButton);

        // add both panes
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        pack();
        setLocationRelativeTo(frame);
    }

    // called when a button is clicked
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Connect".equals(e.getActionCommand()) && list.getSelectedValue()!=null) {
            BonjourList.value = services.get(list.getSelectedValue());
        } else
        	BonjourList.value = null;
        
        BonjourList.dialog.setVisible(false);
    }

	@Override
	public void serviceCallback(int interfaceNum, Protocol proto, BrowserEvent browserEvent,
			String name, String type, String domain, int lookupResultFlag) {

		// only if it's a new service, resolve it
		if(browserEvent==BrowserEvent.NEW){
			try {
				// ServiceResolvers are kept open and a reference is stored
				// in a list so they can be freed upon exit
				resolvers.add(client.createServiceResolver(this, 
						interfaceNum, proto, name, type, domain, 
						Protocol.ANY, 0));
			} catch (Avahi4JException e) {
				System.out.println("error creating resolver");
				e.printStackTrace();
			}
		} else if (browserEvent==BrowserEvent.REMOVE){
			// remove the service from the list
			services.remove(name);
			list.setListData(services.keySet().toArray());
			if(services.size()==0)
				connectButton.setEnabled(false);
		}
	}

	@Override
	public void resolverCallback(ServiceResolver resolver, int interfaceNum, 
			Protocol proto,	ServiceResolverEvent resolverEvent, String name, 
			String type, String domain, String hostname, Address address, 
			int port, String txtRecords[], int lookupResultFlag) {
		
		if(resolverEvent==ServiceResolverEvent.RESOLVER_FOUND) {
			
			if(name==null && type==null && hostname==null) {
				// if null, the service has disappeared, release the resolver
				// and remove it from the list
				resolver.release();
				resolvers.remove(resolver);
			} else {
				// add/replace the service to service maps
				services.put(name, address.getAddress());
				list.setListData(services.keySet().toArray());
				connectButton.setEnabled(true);
			}
		}
	}
}

