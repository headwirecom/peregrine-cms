package com.peregrine.admin.security;

import com.peregrine.admin.resource.NodeNameValidation;
import org.apache.sling.api.resource.LoginException;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public interface PackageValidator {

    boolean isPackageSafe(File packageFile) throws IOException;
}
