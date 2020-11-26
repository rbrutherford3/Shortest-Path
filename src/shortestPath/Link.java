package shortestPath;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

// This class represents a "link" between two nodes
public class Link {
	
	// Node ends
	private Node node1;
	private Node node2;
	
	// Geometric constants
	private Polygon outline;
	private Integer radius = 4;
	
	// Default link state
	private LinkState state = LinkState.INACTIVE;
	
	// Placeholder for a function (see "hightlight" and "needsRepaint" methods)
	private boolean changed;
	
	// Define transparent "color" for inactive links
	private Color transparent = new Color(0f,0f,0f,0f);
	
	// Link can only exist to connect two nodes
	// (geometry of a link with any thickness is complex)
	public Link(Node Node1, Node Node2) {
		
		// Define node ends
		node1 = Node1;
		node2 = Node2;
		
		// Declare x & y values for the 4 corners of the polygon graphic
		//  (these four corners form an angled rectangle)
		int[] xVals = {corner1x(), corner2x(), corner3x(), corner4x()};
		int[] yVals = {corner1y(), corner2y(), corner3y(), corner4y()};
		
		// Define polygon itself
		outline = new Polygon(xVals, yVals, 4);
		
		// tbd
		changed = false;
	}
	
	// "GET" METHODS
	
	// Get length of link (NOT LINK GRAPHIC) using Pythagorean Theorem
	public Double getLength() {
		return Math.sqrt(Math.pow(node2.getX() - node1.getX(), 2) + Math.pow(node2.getY() - node1.getY(), 2));
	}

	// Provide node at one end
	public Node getBegin() {
		return node1;
	}
	
	// Provide node at other end
	public Node getEnd() {
		return node2;
	}
	
	// Provide y-axis displacement of link
	private int delY() {
		return node2.getY()-node1.getY();
	}
	
	// Provide x-axis displacement of link
	private int delX() {
		return node2.getX()-node1.getX();
	}
	
	// Get slope of link
	private Double slope() {
		return ((double) delY())/((double) delX());
	}
	
	// Determine x' (x-axis difference between an end node and
	// a corner of polygon graphic) using trig identities
	private Double primeX() {
		double m = slope();
		return Math.abs(m*radius/(Math.sqrt(Math.pow(m, 2)+1)));
	}
	
	// Determine y' (y-axis difference between an end node and
	// a corner of polygon graphic) using trig identities
	private Double primeY() {
		double m = slope();
		double prime = Math.abs(radius/(Math.sqrt(Math.pow(m, 2)+1)));
		if (m > 0)
			return -1*prime;
		else
			return prime;
	}

	// Determine specific coordinate value of one of the polygon corners
	
	private int corner1x() {
		return (int) Math.round(node1.getX()-primeX());
	}
	
	private int corner1y() {
		return (int) Math.round(node1.getY()-primeY());
	}
	
	private int corner2x() {
		return (int) Math.round(node1.getX()+primeX());
	}
	
	private int corner2y() {
		return (int) Math.round(node1.getY()+primeY());
	}	
	
	private int corner3x() {
		return (int) Math.round(node2.getX()+primeX());
	}
	
	private int corner3y() {
		return (int) Math.round(node2.getY()+primeY());
	}
	
	private int corner4x() {
		return (int) Math.round(node2.getX()-primeX());
	}
	
	private int corner4y() {
		return (int) Math.round(node2.getY()-primeY());
	}
	
	// "DRAW" METHODS
	
	// Draw actual graphic representation of link, given it's current state
	// (see also LinkState.java).  Note that the default state of a link
	// is "inactive" and it's illustration is transparent
	public void draw(Graphics g) { 
		Graphics2D g2 = (Graphics2D) g;
		switch (state) {
		case INACTIVE:
			g2.setColor(transparent);
			break;
		case HIGHLIGHTED:
			g2.setColor(Color.YELLOW);
			break;
		case ACTIVE:
			g2.setColor(Color.BLACK);
			break;
		case PATHLEG:
			g2.setColor(Color.CYAN);
			break;
		}
		g2.fillPolygon(outline);
	}
	
	// Is the cursor over the polygon?
	public boolean inZone(Point point) {
		return outline.contains(point);
	}
	
	// Highlight the link graphic if the mouse cursor is over it.
	// Note the need to save the state in order to restore it
	// once the mouse cursor moves away.  Also note that a state
	// change is necessarily recorded so the canvas only has to
	// be redrawn if a change occurs to save canvas from flickering.
	// (see also LinkState.java)
	public void highlight(Point point) {
		LinkState originalState = state;
		if (state != LinkState.ACTIVE) {
			if (inZone(point))
				state = LinkState.HIGHLIGHTED;
			else
				state = LinkState.INACTIVE;
		}
		if (originalState != state)
			changed = true;
	}
	
	// This function works hand-in-hand with the above "highlight" function
	// to refresh the canvas only if there was a change to or from highlighted 
	// and not every time the mouse moves.  As said, it prevents flickering
	public boolean needsRepaint() {
		boolean changed0 = changed;
		changed = false;
		return changed0;
	}
	
	// Path highlighting occurs after shortest path calculation and
	// is different from the highlighting from mouse hovering
	public void highlightPath() {
		this.state = LinkState.PATHLEG;
	}
	
	// Remove path highlighting
	public void dehighlightPath() {
		this.state = LinkState.ACTIVE;
	}
	
	// Mark link as active (used for functions)
	public void activate() {
		state = LinkState.ACTIVE;
	}
	
	// Activate only if highlighted (for user click action only)
	public void activateFromClick(boolean activate) {
		if (activate && (state == LinkState.HIGHLIGHTED))
			state = LinkState.ACTIVE;
		else
			state = LinkState.INACTIVE;
	}
	
	// Is link active?
	public boolean isActive() {
		return (state == LinkState.ACTIVE);
	}
	
	// Return the node at the other end, given node at "this" end
	public Node getOther(Node node) {
		if (node == node1)
			return node2;
		else if (node == node2)
			return node1;
		return null;
	}
}
