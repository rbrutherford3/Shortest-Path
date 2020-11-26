package shortestPath;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

// This class represents a "node" (a point in the space of the drawing canvas)
public class Node {
	
	// Position:
	private Integer x;
	private Integer y;
	
	// "Box" dimensions (for click area)
	private Integer xmin;
	private Integer xmax;
	private Integer ymin;
	private Integer ymax;
	
	// Diameter of circle graphic
	private Integer diameter = 10;
	
	// Node state (starts off as "Normal," other possibilities
	// include "Start" and "Finish" modes
	// (see also NodeState.java)
	private NodeState state = NodeState.NORMAL;
	
	// All the links that connect this node to other nodes
	private ArrayList<Link> links = new ArrayList<Link>();
	
	// New nodes must have position
	public Node(Integer newX, Integer newY) {
		x = newX;
		y = newY;
	}
	
	// "GET" METHODS
	
	// Provide horizontal position
	public Integer getX() {
		return x;
	}
	
	// Provide vertical position
	public Integer getY() {
		return y;
	}
	
	// Reveal state of node (see also NodeState.java)
	public NodeState getState() {
		return state;
	}
	
	// List all links connecting this node to other nodes
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	// List only active links connecting this node to other nodes
	public ArrayList<Link> getActiveLinks() {
		ArrayList<Link> output = new ArrayList<Link>();
		for (Link link : links) if (link.isActive()) output.add(link);
		return output;
	}
	
	// Is the cursor over the circle?
	public boolean inZone(Point point) {
		int xCheck = point.x;
		int yCheck = point.y;
		return ((xCheck >= xmin) && (yCheck >= ymin) && (xCheck <= xmax) && (yCheck <= ymax));
	}
	
	// "SET" METHODS
	
	// Set horizontal position
	public void setX(Integer xNew) {
		x = xNew;
	}
	
	// Set vertical position
	public void setY(Integer yNew) {
		y = yNew;
	}
	
	// Change state of node (see also NodeState.java)
	public void setState(NodeState stateNew) {
		state = stateNew;
	}
	
	// Add a link to the node
	public void addLink(Link link) {
		links.add(link);
	}
	
	// Remove a link from the node
	public void removeLink(Link link) {
		links.remove(link);
	}
	
	// "DRAW" METHOD
	
	// Draw circle representation of node on canvas
	public void draw(Graphics g) {
		
		// Create "box" around node (click area) using the diameter
		// of the circle graphic representation
		xmin = this.getX() - (int)(Math.floor(diameter/2));
		ymin = this.getY() - (int)(Math.floor(diameter/2));
		if (diameter%2 == 0) {
			xmax = this.getX() + (diameter/2) - 1;
			ymax = this.getY() + (diameter/2) - 1;
		}
		else {
			xmax = this.getX() + (int)(Math.floor(diameter/2));
			ymax = this.getY() + (int)(Math.floor(diameter/2));
		}
		
		// Define circle color (different colors of the different states)
		//  (see also NodeState.java)
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));	// 2 pixel black circle outline
		switch (state) {
		case NORMAL:
			g2.setColor(Color.BLUE);
			break;
		case START:
			g2.setColor(Color.GREEN);
			break;
		case FINISH:
			g2.setColor(Color.RED);
			break;
		}
		
		// Actually draw circle on canvas (fillOval = interior colored circle, drawOval = black circle outline)
		g2.fillOval(this.getX() - (int)(Math.floor(diameter/2)), this.getY() - (int)(Math.floor(diameter/2)), 
				diameter, diameter);
		g2.setColor(Color.BLACK);
		g2.drawOval(this.getX() - (int)(Math.floor(diameter/2)), this.getY() - (int)(Math.floor(diameter/2)), 
				diameter, diameter);	// Must do outline after fill because then the outline will overlay the fill)
	}

}
