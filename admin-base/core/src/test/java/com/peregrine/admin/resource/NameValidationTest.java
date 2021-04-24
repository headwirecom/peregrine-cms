package com.peregrine.admin.resource;

import org.junit.Test;

import static junitx.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NameValidationTest {

// Allowed names: lowercase, numbers, dashes
    String[] validHomepageExamples = new String[]{"hello","1hello","hello221","-1-hello","hello-123123456-goodbye-"};
// Disallowed homepage names: contain spaces, special characters, slashes, tilda's, uppercase, only dashes, more than 40 characters
    String[] inValidHomepageExamples = new String[]{
        " hello ",
        "hwa?",
        ":javascript{}",
        "hello/123",
        "hello#$",
        "\rhello",
        "----",
        "Hello",
        "a1",
        "~hello",
        "really-long-names-are-over-forty-characters-"};

    @Test
    public void userHomepageNames() {
        NodeNameValidation validator = new NodeNameValidationService();
        for(String name : validHomepageExamples){
            assertTrue(validator.isValidUserHomepageName(name));
        }
        for(String name : inValidHomepageExamples){
            assertFalse(validator.isValidUserHomepageName(name));
        }
    }
}
