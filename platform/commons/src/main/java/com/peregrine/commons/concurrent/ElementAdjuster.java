package com.peregrine.commons.concurrent;

import java.util.Collection;

public interface ElementAdjuster<Element> {

	Element findSuperElement(Element newElement, Collection<Element> oldElements);

	Collection<Element> findSubElements(Element newElement, Collection<Element> oldElements);

}
