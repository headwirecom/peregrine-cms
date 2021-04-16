package com.peregrine.reference.impl;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;

/**
 * Node Entry of the Folder Tree
 * This represents a JCR Resource in a path
 */
class Node {

	private String segment;
	private List<Node> children;

	/**
	 * Creates a node with a given parent and name
	 *
	 * @param parent  Parent Node of this node. This node will be added as child to this parent here
	 * @param segment Resource Name
	 */
	public Node(Node parent, String segment) {
		setParent(parent);
		if (isEmpty(segment)) {
			throw new IllegalArgumentException("Node Segment must be defined");
		}
		this.segment = segment;
	}

	/**
	 * Resource Name
	 **/
	public String getSegment() {
		return segment;
	}

	/**
	 * Sets the given Node as parent and adds itself as child to that parent **
	 *
	 * @parent Parent Node which cannot be null
	 */
	void setParent(Node parent) {
		if (parent == null) {
			throw new IllegalArgumentException("Parent Node must be defined");
		}
		parent.addChild(this);
	}

	/**
	 * @return A node with the given resource name if found otherwise null
	 **/
	public Node getChild(String segment) {
		Node answer = null;
		if (children != null && isNotEmpty(segment)) {
			for (Node child : children) {
				if (child.getSegment().equals(segment)) {
					answer = child;
					break;
				}
			}
		}
		return answer;
	}

	/**
	 * Adds the given Node as child
	 *
	 * @param child Node to be added as child which cannot be null
	 * @return Child node which is either the given one or the one that is already added as child with that resource name
	 */
	public Node addChild(Node child) {
		if (child == null) {
			throw new IllegalArgumentException("Cannot add undefined child");
		}
		if (children == null) {
			children = new ArrayList<>();
		}
		Node myChild = getChild(child.segment);
		if (myChild == null) {
			children.add(child);
			return child;
		} else {
			return myChild;
		}
	}

	/**
	 * Creates a new child and adds it as child to this node as parent
	 *
	 * @param segment Resource Name of the new child
	 * @return Node that was created and added if not found otherwise the child with the given resource name
	 */
	public Node addChild(String segment) {
		Node child = getChild(segment);
		if (child == null) {
			child = new Node(this, segment);
		}
		return child;
	}

	@Override
	public String toString() {
		return "Node(" + segment + ") {" + (children == null ? "" : children.toString()) + "}";
	}

}
