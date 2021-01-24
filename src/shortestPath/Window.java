package shortestPath;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

// Main program start point.  Main method sets up the window and the window constructor does the rest.
public class Window extends Frame implements WindowListener {

	private static final long serialVersionUID = -72036050766721762L;

	// Restricts recursion used to find shortest distance path to prevent stack overflow
	private static int maxDepth = 10;
	
	// Criteria for a random network:
	private static boolean randomNetwork = false;
	private static int minNodes;
	private static int maxNodes;
	// A percentage in decimal form (ex: 0.1 means 10% of nodes will be active)
	private static int linksPerNode;
	
	// For tracking the shortest path highlighting
	private ArrayList<Link> shortestPath = new ArrayList<>();
	private boolean shortestPathHighlighted = false;
	
	// Entry point for program.  Arguments can be optionally provided to set maxDepth and random network.
	public static void main(String args[]) throws Exception {
		
		// Parse argument strings, if they exist
		int numArgs = args.length;
		
		if (numArgs == 0) {}
		else if (numArgs == 1) {
			maxDepth = Integer.parseInt(args[0]);
		}
		else if (numArgs == 3) {
			randomNetwork = true;
			minNodes = Integer.parseInt(args[1]);
			maxNodes = Integer.parseInt(args[2]);
			linksPerNode = Integer.parseInt(args[3]);
		}
		else if (numArgs == 4) {
			maxDepth = Integer.parseInt(args[0]);
			randomNetwork = true;
			minNodes = Integer.parseInt(args[1]);
			maxNodes = Integer.parseInt(args[2]);
			linksPerNode = Integer.parseInt(args[3]);
		}
		else {
			throw new Exception("Invalid number of arguments");
		}
		
		// Set up GUI
		new Window();
	}

	// Set up GUI
	Window() {
		
		// Set up window
		addWindowListener(this);
	    this.setLayout(null); 
	    this.setSize(500, 600);
	    this.setTitle("Shortest Path finder");
	    
	    // Create checkboxes as radio buttons to change user mode
	    CheckboxGroup cbg = new CheckboxGroup();
	    Checkbox cbEdit = new Checkbox("Create Nodes & Links", cbg, true);
	    Checkbox cbStart = new Checkbox("Select Start Node", cbg, false);
	    Checkbox cbFinish = new Checkbox("Select Final Node", cbg, false);
	    
	    // Create button to find shortest distance path
	    Button submit = new Button("Submit");
	    submit.setEnabled(true);
	    
	    // Set up control panel (radio buttons and submit button)
	    Panel controlPanel = new Panel();
	    controlPanel.add(cbEdit);
	    controlPanel.add(cbStart);
	    controlPanel.add(cbFinish);
	    controlPanel.add(submit);
	    controlPanel.setBounds(0, 500, 500, 50);
	    controlPanel.setBackground(Color.LIGHT_GRAY);
	    
	    // Set up feedback to user
	    Label messageText = new Label("Left-click to add nodes/links, right-click to remove them.  Click and drag nodes to move.", Label.LEFT);
	    messageText.setBounds(50, 550, 400, 50);
	    Panel messagePanel = new Panel();
	    messagePanel.add(messageText);
	    messagePanel.setBounds(0, 550, 500, 50);
	    messagePanel.setBackground(Color.LIGHT_GRAY);
	    
	    // Create display (Display class is the canvas 
	    // object where nodes and links are "drawn")
	    Display display = new Display();
	    Panel displayPanel = new Panel();
	    displayPanel.setBounds(0, 0, 500, 500);
	    displayPanel.setBackground(Color.DARK_GRAY);
	    displayPanel.add(display);
	    
	    // Add all panels to the window
	    add(controlPanel);
	    add(displayPanel);
	    add(messagePanel);
	    
	    // Create a random network of nodes and links, or a blank canvas
	    Network network;
		if (randomNetwork)
	    	network = new Network(minNodes, maxNodes, linksPerNode);
	    else
	    	network = new Network();
		
		// Add this network to the window's display component
		display.update(network);
		
		// Show window, in all it's glory
		this.setVisible(true);
		
		// Set up actions for each of the radio buttons (change modes and provide feedback)
		// Edit network mode: add/delete nodes/links
		ItemListener checkBoxEditListener = new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e) {
				display.setMode(InputState.EDIT_NETWORK);
				messageText.setText("Left-click to add nodes and links, right-click to remove them");
				network.dehighlight(shortestPath);
				display.repaint();
			}
		};
		
		// Pick start mode: select a beginning node for finding shortest distance
		ItemListener checkBoxStartListener = new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e) {
				display.setMode(InputState.PICK_START);
				messageText.setText("Please select a beginning node");
				network.dehighlight(shortestPath);
				display.repaint();
			}
		};
		
		// Pick end mode: select an ending node for finding shortest distance
		ItemListener checkBoxFinishListener = new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e) {
				display.setMode(InputState.PICK_FINISH);
				messageText.setText("Please select an ending node");
				network.dehighlight(shortestPath);
				display.repaint();
			}
		};
		
		// When user clicks "Submit" button...
		ActionListener submitButtonClickListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Check that "start" and "finish" nodes are set...
				boolean startExists = false;
				boolean finishExists = false;
				Node startNode = null;
				ArrayList<Link> path;
				for (Node node : network.getNodes()) {
					if (node.getState() == NodeState.START) {
						startExists = true;
						startNode = node;
					}
					else if (node.getState() == NodeState.FINISH)
						finishExists = true;
				}
				if (startExists && finishExists) {
					
					// Find and display shortest distance through network between "start" and "finish" nodes!
					shortestPath = network.shortestPath(startNode, maxDepth);
					for (Link link : shortestPath) {
						link.highlightPath();
						display.repaint();
						shortestPathHighlighted = true;
					}
				}
				else {
					messageText.setText("Please select a beginning node and an ending node");
				}
			}
			
		};
		
		// Add methods to user input buttons
		cbEdit.addItemListener(checkBoxEditListener);
		cbStart.addItemListener(checkBoxStartListener);
		cbFinish.addItemListener(checkBoxFinishListener);
		submit.addActionListener(submitButtonClickListener);
	}
	
	/*
	 * // Function to remove the shortest path highlighting private void
	 * dehighlight() { System.out.println("ENTERED"); if (shortestPathHighlighted) {
	 * for (Link link : shortestPath) { link.dehighlightPath(); link.needsRepaint();
	 * } repaint(); shortestPathHighlighted = false; } }
	 */
	
	// Exit program upon closing window
	@Override
	public void windowClosing(WindowEvent e) {  
		dispose();
	}

	// Extra unused actions for window
	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

}