package com.peregrine.seo;

/**
 * <p>Service for looking for the canonical page.</p>
 */
public interface CanonicalTagging {

  /**
   * <p>Method, which returns canonical link for the given resource path.</p>
   * <p>Internally, page will be extracted for the provided resource path.
   *      Using rules canonical path will be tried to found.</p>
   * <p>Using Externalizer, path will be converted to the link.</p>
   *
   * @param resourcePath which will be used to get page it is part of
   * @return canonical link or null
   */
  String getCanonicalLink(String resourcePath);
}
