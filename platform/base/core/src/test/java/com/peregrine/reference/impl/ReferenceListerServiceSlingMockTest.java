package com.peregrine.reference.impl;

import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.peregrine.reference.impl.ReferenceListerService.REFERENCE_REGEX_TEMPLATE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReferenceListerServiceSlingMockTest {

    @Rule
    public final SlingContext context = new SlingContext();

    ReferenceListerService service = new ReferenceListerService();

    @Test
    public void testResourceHasStringValueMatchingSimplePredicate() {
        context.load().json("/referenceLister-tree1.json", "/content");
        Predicate<String> pred = "texttext"::equals;
        assertFalse(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res1"), pred));
        assertTrue(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res2"), pred));
        assertFalse(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res3"), pred));
        assertTrue(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res4"), pred));
    }

    @Test
    public void testResourceHasStringValueMatchingRegexPredicate() {
        context.load().json("/referenceLister-tree2.json", "/content");
        Pattern pattern = Pattern.compile(String.format(REFERENCE_REGEX_TEMPLATE, "/content/tenant/pages/index"));
        Predicate<String> pred = s -> pattern.matcher(s).find();
        assertTrue(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res1"), pred));
        assertFalse(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res2"), pred));
        assertTrue(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res3"), pred));
        assertFalse(service.resourceHasStringValueMatchingPredicate(
                context.resourceResolver().getResource("/content/res4"), pred));
    }

}
