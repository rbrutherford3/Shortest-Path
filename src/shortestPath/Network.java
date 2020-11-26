package shortestPath;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

// The Network class holds all the information the nodes and links,
// as well as methods for manipulating each.
public class Network {
	
	// Nodes & Links
	private ArrayList<Node> nodes;
	private ArrayList<Link> links;
	
	// Initialize empty network
	public Network() {
		nodes = new ArrayList<Node>();
		links = new ArrayList<Link>();
	}
	
	// Initialize random network
	public Network(int minNodes, int maxNodes, int linksPerNode) {
		
		// Start with empty network
		nodes = new ArrayList<Node>();
		links = new ArrayList<Link>();
		
		// Initialize randomizer
		Random rand = new Random();
		
		// Add random number of nodes (b/w minNodes and maxNodes at
		// random locations (between x/y=25 and x/y=475
		int numNodes = rand.nextInt(maxNodes-minNodes)+minNodes;
		for (int i=0; i < numNodes; i++) {
			this.addNode(rand.nextInt(450)+25, rand.nextInt(450)+25);
		}
		
		// Empty list of loners (nodes with no links)
		ArrayList<Node> removeThese = new ArrayList<Node>();
		
		// Activate links between nodes at a frequency of linkFreq (b/w 0 and 1)
		// note: since there are two nodes for each link, the frequency is divided by 2
		double linkFreq = Double.valueOf(linksPerNode)/2*Double.valueOf(numNodes)/Double.valueOf(this.getNumLinks());
		
		for (Link link : this.links) {
			double i = rand.nextFloat();
			if (i < linkFreq) link.activate();
		}	
		
		// Build list of orphan nodes
		for (Node node : nodes)
			if (node.getActiveLinks().isEmpty()) removeThese.add(node);
		// Remove orphan nodes
		for (Node node : removeThese) removeNode(node);
	}
	
	// "GET" METHODS
	
	// Provide network nodes
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	// Provide network links
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	// Provide active links only
	public ArrayList<Link> getActiveLinks() {
		ArrayList<Link> output = new ArrayList<Link>();
		for (Link link : links) {
			if (link.isActive()) output.add(link);
		}
		return output;
	}
	
	// Provide number of nodes in network
	public int getNumNodes() {
		return nodes.size();
	}
	
	// Provide number of links in network
	public int getNumLinks() {
		return links.size();
	}
	
	// Find the length of a path (array of links)
	public static double getPathLength(ArrayList<Link> path) {
		double length = 0;
		for (Link link : path)
			length += link.getLength();
		return length;
	}
	
	// "SET" METHODS
	
	// Add a node to the network at a given location
	public Node addNode(int x, int y) {
		Node newNode = new Node(x, y);
		
		// Create links between new node and EVERY OTHER NODE
		// (this allows potential links to be highlighted)
		// Links must be "active" to be of any use in network
		for (Node node : nodes) {
			Link link = new Link(newNode, node);
			links.add(link);
			node.addLink(link);
			newNode.addLink(link);
		}
		
		// Add node to network
		nodes.add(newNode);
		return newNode;	// sometimes it's necessary to return the node
	}
	
	// Move a node (and it's associated links) to a new location
	public void moveNode(Node targetNode, Point point) {
		
		// Get linked nodes where the links are active
		ArrayList<Node> connectedNodes = new ArrayList<Node>();
		for (Link link : targetNode.getActiveLinks())
			connectedNodes.add(link.getOther(targetNode));
		
		// Remove the node from the network, add a new one at new location
		removeNode(targetNode);
		Node newNode = this.addNode(point.x, point.y);
		
		// Activate new links (were active prior to this operation)
		for (Link link : newNode.getLinks()) {
			if (connectedNodes.contains(link.getOther(newNode)))
				link.activate();
		}
	}
	
	// Remove a node (and all links that connected it)
	public void removeNode(Node targetNode) {
		nodes.remove(targetNode);
		removeLink(targetNode);
	}
	
	// Remove all links connected to a node
	public void removeLink(Node targetNode) {
		
		// Tally "orphan links" (links previously connecting with removed node)
		ArrayList<Link> orphans = new ArrayList<Link>();
		for (Link link : links) {
			if ((link.getBegin() == targetNode) || (link.getEnd() == targetNode))
				orphans.add(link);
		}
		
		// Remove individual "orphan" links
		for (Link link : orphans) {
			links.remove(link);
		}
	}
	
	// Deactivate a link
	public void deactivateLink(Link targetLink) {
		for (Link link : links) {
			if (link == targetLink)
				link.activateFromClick(false);
		}
	}
	
	// For a path, "highlight" each link (note that this is different
	// than the highlighting of inactive links in "edit" mode
	public static void highlight(ArrayList<Link> path) {
		for (Link link : path)
			link.highlightPath();
	}
	
	// De-highlight links in a path
	public static void dehighlight(ArrayList<Link> path) {
		for (Link link : path)
			link.dehighlightPath();
	}
	
	// This function is the heart of the program: a recursive algorithm for 
	// finding the shortest path between the given "start" and "finish" nodes,
	// using only active links as part of the path.  The function starts with
	// the first node, traverses each of it's active links (unless the link is
	// already part of the path), called "branches," invoking the function again 
	// for each new branch.  The function returns null if there are no more
	// branches to traverse, or it reaches the end, in which case it returns the
	// branch that got there.  Going "down the stack," the function returns
	// the shortest path found for each branch.  It is easy for the network to
	// get so complex that this recursion becomes entirely inefficient, even with
	// the check for previously traveled links.  As such, a "max depth" variable
	// is available to keep the stack short, as necessary.
	// Arguments: 
	// ArrayList<Link> path - a branch from the "previous" node to build upon
	//    (first iteration would be null, since there is no path yet)
	// Node lastnode - the node connected to the other end of the last link (makes things easier)
	//    (first iteration would be the "start" node)
	// int maxdepth - Limits the recursion level to avoid overflow in larger networks
	public ArrayList<Link> shortestPath(ArrayList<Link> path, Node lastNode, int maxDepth) {
		// Halt recursion if maxdepth is met, record depth otherwise:
		if (maxDepth < 0)
			return null;
		else
			maxDepth--;
		// Return a path that has found it's way to the end
		if (lastNode.getState() == NodeState.FINISH) {
			return path;
		}
		// Compare distance of branches, return the shortest one found
		else {
			ArrayList<Link> shorterPath = null;
			
			// Get branches (links connected to the last node...
			for (Link link : lastNode.getActiveLinks()) {
				ArrayList<Link> branch = new ArrayList<Link>();
				ArrayList<Link> considerThis = null;
				boolean beenThere = false;
				if (path == null)
					branch.add(link);
				else {
					// IMPORTANT: without ignoring links already traveled,
					// the function could easily get lost in a loop
					if (path.contains(link))
						beenThere = true;
					else {
						branch = new ArrayList<Link>(path);
						branch.add(link);
					}
				}
				// To find shortest distance to "finish," more instances of the function
				// are called.  Each "branch" will return either a 'null' (could not
				// find "finish" node) or a path to the "finish" node.  The shortest of these
				// paths is returned (or null, if not found)
				if (!beenThere) {
					considerThis = shortestPath(branch, link.getOther(lastNode), maxDepth);
					if (considerThis != null) {
						if (shorterPath == null)
							shorterPath = new ArrayList<Link>(considerThis);
						else {
							if (getPathLength(considerThis) < getPathLength(shorterPath))
								shorterPath = new ArrayList<Link>(considerThis);
						}
					}
				}
			}
			return shorterPath;
		}
	}
	
	// Call shortest path function without having a path to start with
	// (start with "Start" node)
	public ArrayList<Link> shortestPath(Node lastNode, int maxDepth) {
		return shortestPath(null, lastNode, maxDepth);
	}
}