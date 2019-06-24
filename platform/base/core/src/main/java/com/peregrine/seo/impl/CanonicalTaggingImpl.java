package com.peregrine.seo.impl;

import com.peregrine.seo.CanonicalTagging;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = CanonicalTagging.class)
public class CanonicalTaggingImpl implements CanonicalTagging {

  private static final Logger log = LoggerFactory.getLogger(CanonicalTaggingImpl.class);

  @Override
  public String getCanonicalLink(String resourcePath) {
    return null;
  }
}
