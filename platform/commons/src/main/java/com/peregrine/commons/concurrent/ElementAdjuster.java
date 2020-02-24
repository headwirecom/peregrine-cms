package com.peregrine.commons.concurrent;

import java.util.Set;

public interface ElementAdjuster<Element> {

	Element findSuperElement(Element newElement, Set<Element> oldElements);

	Set<Element> findSubElements(Element newElement, Set<Element> oldElements);

}
