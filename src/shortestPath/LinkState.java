package shortestPath;

// Constants that define the state of a link
public enum LinkState {
	INACTIVE,		// Not active (invisible)
	HIGHLIGHTED,	// Mouse hovering over inactive link to reveal it
	ACTIVE,			// Active links form a real connection b/w nodes
	PATHLEG			// Active links that form a path (shortest path)
}
