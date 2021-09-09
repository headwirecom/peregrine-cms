package com.peregrine.admin.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.peregrine.admin.resource.NodeNameValidation;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.fs.api.FilterSet;
import org.apache.jackrabbit.vault.fs.api.VaultInputSource;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.config.MetaInf;
import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
import org.apache.jackrabbit.vault.packaging.impl.ZipVaultPackage;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerUtil.loginService;

@Component
public class PackageValidatorService implements PackageValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private Packaging packaging;

    @Reference
    private NodeNameValidation nodeNameValidation;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    private static final List<String> DEFAULT_ROOT_PREFIXES = List.of(
            "/apps/",
            "/etc/felibs/",
            "/content/");

    public boolean isPackageSafe(File packageFile) throws IOException {
        try (VaultPackage jcrPackage = packaging.getPackageManager().open(packageFile)) {

            List<String> storedRoots = getStoredRoots(jcrPackage);
            if (storedRoots == null) {
                return false;
            }

            WorkspaceFilter filter = jcrPackage.getMetaInf().getFilter();
            if (storedRoots.isEmpty()) {
                return isFilterSetPermitted(filter);
            } else {
                return isFilterConsistentWithExistingRoots(filter, storedRoots);
            }
        }
    }

    private boolean isPackageSafe(InputStream is) throws IOException {
        try (VaultPackage jcrPackage = packaging.getPackageManager().open(is)) {

            List<String> storedRoots = getStoredRoots(jcrPackage);
            if (storedRoots == null) {
                return false;
            }

            WorkspaceFilter filter = jcrPackage.getMetaInf().getFilter();
            if (storedRoots.isEmpty()) {
                return isFilterSetPermitted(filter);
            } else {
                return isFilterConsistentWithExistingRoots(filter, storedRoots);
            }
        }
    }

    @Nullable
    private List<String> getStoredRoots(VaultPackage jcrPackage) {
        try (ResourceResolver resolver = loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME)) {

            PackageId packageId = jcrPackage.getMetaInf().getPackageProperties().getId();
            String packageLocation = String.format("/etc/packages/%s/%s-%s.zip",
                    packageId.getGroup(),
                    packageId.getName(),
                    packageId.getVersion());

            Resource definedFilters = resolver.getResource(packageLocation + "/jcr:content/vlt:definition/filter");

            return  Optional.ofNullable(definedFilters)
                    .map(Resource::getChildren)
                    .map(Iterable::spliterator)
                    .map(it -> StreamSupport.stream(it, false))
                    .orElseGet(Stream::empty)
                    .map(Resource::getValueMap)
                    .map(vm -> vm.get("root", StringUtils.EMPTY))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        } catch (LoginException e) {
            logger.error("Could not obtain a service resource resolver, package marked as unsafe just in case", e);
            return null;
        }
    }

    @VisibleForTesting
    boolean isFilterConsistentWithExistingRoots(WorkspaceFilter filter, List<String> existingRoots) {
        return extractRootsFromFilter(filter).stream()
                .allMatch(root -> existingRoots
                        .stream()
                        .anyMatch(root::equals));
    }

    @VisibleForTesting
    List<String> extractRootsFromFilter(WorkspaceFilter filter) {
        return filter.getFilterSets()
                .stream()
                .map(FilterSet::getRoot)
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    boolean isFilterSetPermitted(WorkspaceFilter filter) {
        List<String> packageRoots = extractRootsFromFilter(filter);

        return allRootsArePermitted(packageRoots, DEFAULT_ROOT_PREFIXES)
                && hasSinglePermittedSuffix(packageRoots, DEFAULT_ROOT_PREFIXES);
    }

    @VisibleForTesting
    boolean allRootsArePermitted(List<String> roots, List<String> permittedRootPrefixes) {
        return roots.stream()
                .allMatch(root -> permittedRootPrefixes
                        .stream()
                        .anyMatch(root::startsWith));
    }

    @VisibleForTesting
    boolean hasSinglePermittedSuffix(List<String> roots, List<String> permittedRootPrefixes) {
        Set<String> suffixes = roots.stream().map(
                        root -> {
                            String permittedPath = permittedRootPrefixes.stream()
                                    .filter(root::startsWith)
                                    .findFirst()
                                    .orElse(StringUtils.EMPTY);
                            return StringUtils.substringAfter(root, permittedPath);
                        })
                .collect(Collectors.toSet());

        return suffixes.size() == 1 &&
                nodeNameValidation.isValidSiteName(Iterables.get(suffixes, 0));
    }
}
