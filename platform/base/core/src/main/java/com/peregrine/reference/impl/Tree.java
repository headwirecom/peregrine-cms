package com.peregrine.reference.impl;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;

/**
 * Root Object of a Folder Name Base Tree. It allows to
 * add children by a JCR Node Path
 **/
final class Tree extends Node {

	public Tree() {
		super(null, SLASH);
	}

	void setParent(Node parent) {}

	/**
	 * Checks if the given path exists in this tree
	 *
	 * @param path JCR Resource Path to be checked
	 * @return True if for all JCR resources of the path there is a corresponding node
	 */
	public boolean contains(String path) {
		boolean answer = false;
		if (isNotEmpty(path)) {
			answer = true;
			String[] tokens = path.split(SLASH);
			Node node = this;
			for (String token : tokens) {
				if (isNotEmpty(token)) {
					Node child = node.getChild(token);
					if (child != null) {
						node = child;
					} else {
						answer = false;
						break;
					}
				}
			}
		}
		return answer;
	}

	/**
	 * Creates a node for a resources in the given path if they don't already exist
	 *
	 * @param path JCR Resource Path separated by a slash
	 * @return This tree instance
	 */
	public Tree addChildByPath(String path) {
		if (isEmpty(path)) {
			throw new IllegalArgumentException("Child Path must be provided");
		}
		String[] tokens = path.split(SLASH);
		Node node = this;
		for (String token : tokens) {
			if (isNotEmpty(token)) {
				Node child = node.getChild(token);
				if (child != null) {
					node = child;
				} else {
					node = node.addChild(token);
				}
			}
		}
		return this;
	}

	@Override
	public String toString() {
		return "Tree(" + super.toString() + ")";
	}

}
