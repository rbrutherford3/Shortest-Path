package shortestPath;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// Canvas object which will contain all the graphic elements for the network
class Display extends Canvas {

	private static final long serialVersionUID = 6665209011564835685L;
	
	private Network network;
	private InputState mode = InputState.EDIT_NETWORK;	// "edit" mode is default
	
	// Drawing environment constants
	private int CanvasWidth = 500;
	private int CanvasHeight = 500;
	private Color BackgroundColor = Color.GRAY;
	
	// Buffered graphics objects
	private GraphicsConfiguration graphicsconfiguration;
	private BufferedImage bufferedimage;
	private Graphics bufferedgraphics;
	
	// Node to be moved from one location to another (see below)
	// private Node moveNode = null; 
	
	public Display() {
		
		// Set up canvas
		setBackground (BackgroundColor);
		setSize(CanvasWidth, CanvasHeight);
		
		// Set up buffered graphics
		graphicsconfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		bufferedimage = graphicsconfiguration.createCompatibleImage(CanvasWidth, CanvasHeight);
		bufferedgraphics = bufferedimage.getGraphics();
		
		addMouseListener(new MouseListener(){
			
			// Whenever the user clicks a mouse button...
			@Override
			public void mouseClicked(MouseEvent e){
				
				// Save network locally
				ArrayList<Node> nodes = network.getNodes();
				ArrayList<Link> links = network.getLinks();
				Point point = e.getPoint(); // Get mouse coordinates
				
				// Different modes (set by radio buttons) have different
				// actions for mouse button clicks
				switch(mode) {
				
				// For "edit network" mode, left mouse clicks should add
				// nodes/links and right mouse clicks should remove them.
				case EDIT_NETWORK:
					// If mouse is hovering over a possible link, then left
					// clicking will add the link.  Otherwise, a node is added
					if (e.getButton() == MouseEvent.BUTTON1) {
						boolean linkFound = false;
						for (Link link : links) {
							if (link.inZone(point)) {
									link.activateFromClick(true);
									linkFound = true;
							}
						}
						if (!linkFound)
							network.addNode(e.getX(), e.getY());
					}
					// If mouse is hovering over an active link, then right
					// clicking will de-activate the link.  For nodes: deletion.
					else {
						Node trashNode = null;
						boolean removeNode = false;
						for (Node node : nodes) {
							if (node.inZone(point)) {
								removeNode = true;
								trashNode = node;
							}
						}
						if (removeNode)
							network.removeNode(trashNode);
						else {
							for (Link link : links) {
								if (link.inZone(point))
									link.activateFromClick(false);
							}
						}
					}
					break;
				// In "pick start" mode, clicking the left mouse button
				// will select that node as the beginning of the path.
				case PICK_START:
					for (Node node : nodes) {
						if (node.inZone(point)) {
							if (e.getButton() == MouseEvent.BUTTON1)
								node.setState(NodeState.START);
							else
								node.setState(NodeState.NORMAL);
						}
						// Make sure that "old" start node is demoted
						else if (node.getState() == NodeState.START)
							node.setState(NodeState.NORMAL);
					}
					break;
				// In "pick finish" mode, clicking the left mouse button
				// will select that node as the end of the path.
				case PICK_FINISH:
					for (Node node : nodes) {
						if (node.inZone(point)) {
							if (e.getButton() == MouseEvent.BUTTON3)
								node.setState(NodeState.NORMAL);
							else	
								node.setState(NodeState.FINISH);
						}
						// Make sure that "old" finish node is demoted
						else if (node.getState() == NodeState.FINISH) {
							node.setState(NodeState.NORMAL);
						}
					}
					break;
				}
				// Refresh canvas
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
			// THIS RIDE IS TEMPORARILY CLOSED (SEE U LATER):
			// The following two mouse events (press and release)
			// are used to allow nodes to be moved after placement.
			// Class variable "moveNode" is used to save info while
			// dragging mouse.
//			@Override
//			public void mousePressed(MouseEvent e) {
//				ArrayList<Node> nodes = network.getNodes();
//				Point point = e.getPoint();
//				for (Node node : nodes)
//					if (node.inZone(point))
//						moveNode = node;
//			}
//			
//			@Override
//			public void mouseReleased(MouseEvent e) {
//				network.moveNode(moveNode, e.getPoint());
//			}
		});
		
		addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent e) {
				// May use this function to show node
				// being moved, but not yet
			}

			// When hovering over an inactive (and invisible) link,
			// the potential link is highlighted as a visual cue
			@Override
			public void mouseMoved(MouseEvent e) {
				if (mode == InputState.EDIT_NETWORK) {
					ArrayList<Link> links = network.getLinks();
					Point point = e.getPoint();
					for (Link link : links) {
						link.highlight(point);
						// The canvas must be redrawn each time a new
						// link is highlighted.  The "needsRepaint()"
						// function allows those refreshes to occur
						// only when necessary to reduce screen flicker
						if (link.needsRepaint())
							repaint();
					}
				}
			}
		});
	}
	
	// Called by the radio button click listeners to change modes
	public void setMode(InputState modeNew) {
		mode = modeNew;
	}

	// Used by the Window object to join the network to this display
	public void update(Network n) {
		network = n;
	}
	
	// Whenever the canvas needs to be redrawn, then each of the network
	// elements (nodes and links) must also be redrawn.  The code for
	// displaying nodes and links lie in their respective classes.
	// This uses buffered graphics (develop in the background, show in
	// foreground when done).
	@Override
	public void paint(Graphics g)
	{
		// Set up hidden canvas buffer
		bufferedgraphics.clearRect(0, 0, CanvasWidth, CanvasHeight);
		bufferedgraphics.setColor(BackgroundColor);
		bufferedgraphics.fillRect(0,0,CanvasWidth, CanvasHeight);
		
		// Draw individual node and link objects, build into buffer
		if (network != null) {
			ArrayList<Link> links = network.getLinks();
			for (int i=0; i<links.size(); i++) {
				Link link = links.get(i);
				link.draw(bufferedgraphics);
			}
			ArrayList<Node> nodes = network.getNodes();
			for (int i=0; i<nodes.size(); i++) {
				Node node = nodes.get(i);
				node.draw(bufferedgraphics);
			}
		}
		
		// Display the buffered objects in the foreground, after calcs
		g.drawImage(bufferedimage,0,0,this);
	}
	
	// Automatically called when needed, in this case, whenever the mouse moves
	public void update(Graphics g) {
		paint(g);
	}
}
