package com.peregrine.admin.security;

import com.peregrine.admin.resource.NodeNameValidationService;
import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.packaging.impl.PackagingImpl;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PackageValidatorServiceTest {

    SlingContext context = new SlingContext(ResourceResolverType.JCR_MOCK);

    PackageValidatorService packageValidatorService;

    @Before
    public void setUp() throws LoginException {
        context.registerService(Packaging.class, new PackagingImpl());
        ResourceResolver resourceResolver = context.resourceResolver();
        ResourceResolverFactory resourceResolverFactory = mock(ResourceResolverFactory.class);
        when(resourceResolverFactory.getAdministrativeResourceResolver(any())).thenReturn(resourceResolver);
        context.registerService(ResourceResolverFactory.class, resourceResolverFactory);
        context.registerInjectActivateService(NodeNameValidationService.class);
        packageValidatorService = context.registerInjectActivateService(PackageValidatorService.class);
    }

    @Test
    public void testSinglePermittedSuffix() {
        assertTrue(packageValidatorService.hasSinglePermittedSuffix(
                List.of("/a/a", "/b/a"),
                List.of("/a/", "/b/")));
        assertTrue(packageValidatorService.hasSinglePermittedSuffix(
                List.of("/a/a", "/b/a", "/c/d/a"),
                List.of("/a/", "/b/", "/c/d/")));
        assertFalse(packageValidatorService.hasSinglePermittedSuffix(
                List.of("/a/a", "/b/a", "/c/d/a"),
                List.of("/a/", "/b/", "/c/")));
        assertFalse(packageValidatorService.hasSinglePermittedSuffix(
                List.of("/a/a", "/b/a", "/c/a"),
                List.of("/a/", "/b/")));
        assertFalse(packageValidatorService.hasSinglePermittedSuffix(
                List.of("/a/ a", "/b/ a"),
                List.of("/a/", "/b/")));
        assertFalse(packageValidatorService.hasSinglePermittedSuffix(
                List.of("/a/a", "/b/b"),
                List.of("/a/", "/b/")));
    }


    @Test
    public void testAllRootsArePermitted() {
        assertTrue(packageValidatorService.allRootsArePermitted(
                List.of("/a/a", "/b/c"),
                List.of("/a/", "/b/")));
        assertTrue(packageValidatorService.allRootsArePermitted(
                List.of("/a/a"),
                List.of("/a/", "/b/")));
        assertFalse(packageValidatorService.allRootsArePermitted(
                List.of("/a/a", "/b/c", "/c/d"),
                List.of("/a/", "/b/")));
    }

    @Test
    public void testFilterSetPermittedOk() {
        DefaultWorkspaceFilter workspaceFilter = new DefaultWorkspaceFilter();
        workspaceFilter.add(new PathFilterSet("/apps/tenant"));
        workspaceFilter.add(new PathFilterSet("/etc/felibs/tenant"));
        workspaceFilter.add(new PathFilterSet("/content/tenant"));
        assertTrue(packageValidatorService.isFilterSetPermitted(workspaceFilter));
    }

    @Test
    public void testFilterSetPermittedWrong1() {
        DefaultWorkspaceFilter workspaceFilter = new DefaultWorkspaceFilter();
        workspaceFilter.add(new PathFilterSet("/apps/themecleanflex"));
        workspaceFilter.add(new PathFilterSet("/etc/felibs/themecleanflex"));
        workspaceFilter.add(new PathFilterSet("/content/themecleanflex"));
        assertFalse(packageValidatorService.isFilterSetPermitted(workspaceFilter));
    }

    @Test
    public void testFilterSetPermittedWrong2() {
        DefaultWorkspaceFilter workspaceFilter = new DefaultWorkspaceFilter();
        workspaceFilter.add(new PathFilterSet("/apps/tenant"));
        workspaceFilter.add(new PathFilterSet("/etc/felibs/tenant"));
        workspaceFilter.add(new PathFilterSet("/content/tenant"));
        workspaceFilter.add(new PathFilterSet("/libs"));
        assertFalse(packageValidatorService.isFilterSetPermitted(workspaceFilter));
    }

    @Test
    public void testIsFilterConsistentWithExistingRootsOK() {
        DefaultWorkspaceFilter workspaceFilter = new DefaultWorkspaceFilter();
        workspaceFilter.add(new PathFilterSet("/apps/tenant"));
        workspaceFilter.add(new PathFilterSet("/etc/felibs/tenant"));
        workspaceFilter.add(new PathFilterSet("/content/tenant"));
        assertTrue(packageValidatorService.isFilterConsistentWithExistingRoots(workspaceFilter,
                List.of("/content/tenant", "/etc/felibs/tenant", "/apps/tenant")));
    }

    @Test
    public void testIsFilterConsistentWithExistingRootsWrong() {
        DefaultWorkspaceFilter workspaceFilter = new DefaultWorkspaceFilter();
        workspaceFilter.add(new PathFilterSet("/apps/tenant"));
        workspaceFilter.add(new PathFilterSet("/etc/felibs/tenants"));
        workspaceFilter.add(new PathFilterSet("/content/tenant"));
        assertFalse(packageValidatorService.isFilterConsistentWithExistingRoots(workspaceFilter,
                List.of("/content/tenant", "/etc/felibs/tenant", "/apps/tenant")));
    }

    @Test
    public void testPackage() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/tenant-full-package-1.0.zip");
        assertTrue(packageValidatorService.isPackageSafe(is));
    }

    @Test
    public void testPackageWithSubpackage() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/tenant-full-package-with-subpackage-1.0.zip");
        assertFalse(packageValidatorService.isPackageSafe(is));
    }


}