package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
*/
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final KotestLibraryAccessors laccForKotestLibraryAccessors = new KotestLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(providers, config);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers) {
        super(config, providers);
    }

    /**
     * Returns the group of libraries at kotest
     */
    public KotestLibraryAccessors getKotest() { return laccForKotestLibraryAccessors; }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() { return vaccForVersionAccessors; }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() { return baccForBundleAccessors; }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() { return paccForPluginAccessors; }

    public static class KotestLibraryAccessors extends SubDependencyFactory {
        private final KotestAssertionsLibraryAccessors laccForKotestAssertionsLibraryAccessors = new KotestAssertionsLibraryAccessors(owner);
        private final KotestJunit5LibraryAccessors laccForKotestJunit5LibraryAccessors = new KotestJunit5LibraryAccessors(owner);

        public KotestLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at kotest.assertions
         */
        public KotestAssertionsLibraryAccessors getAssertions() { return laccForKotestAssertionsLibraryAccessors; }

        /**
         * Returns the group of libraries at kotest.junit5
         */
        public KotestJunit5LibraryAccessors getJunit5() { return laccForKotestJunit5LibraryAccessors; }

    }

    public static class KotestAssertionsLibraryAccessors extends SubDependencyFactory {
        private final KotestAssertionsCoreLibraryAccessors laccForKotestAssertionsCoreLibraryAccessors = new KotestAssertionsCoreLibraryAccessors(owner);

        public KotestAssertionsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at kotest.assertions.core
         */
        public KotestAssertionsCoreLibraryAccessors getCore() { return laccForKotestAssertionsCoreLibraryAccessors; }

    }

    public static class KotestAssertionsCoreLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public KotestAssertionsCoreLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (io.kotest:kotest-assertions-core)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() { return create("kotest.assertions.core"); }

            /**
             * Creates a dependency provider for jvm (io.kotest:kotest-assertions-core-jvm)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJvm() { return create("kotest.assertions.core.jvm"); }

    }

    public static class KotestJunit5LibraryAccessors extends SubDependencyFactory {

        public KotestJunit5LibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jvm (io.kotest:kotest-runner-junit5-jvm)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJvm() { return create("kotest.junit5.jvm"); }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotest (5.5.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKotest() { return getVersion("kotest"); }

            /**
             * Returns the version associated to this alias: kotlin (1.7.20)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKotlin() { return getVersion("kotlin"); }

    }

    public static class BundleAccessors extends BundleFactory {
        private final KotlinBundleAccessors baccForKotlinBundleAccessors = new KotlinBundleAccessors(providers, config);

        public BundleAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of bundles at bundles.kotlin
         */
        public KotlinBundleAccessors getKotlin() { return baccForKotlinBundleAccessors; }

    }

    public static class KotlinBundleAccessors extends BundleFactory {

        public KotlinBundleAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a dependency bundle provider for kotlin.testing which is an aggregate for the following dependencies:
             * <ul>
             *    <li>io.kotest:kotest-runner-junit5-jvm</li>
             *    <li>io.kotest:kotest-assertions-core</li>
             *    <li>io.kotest:kotest-assertions-core-jvm</li>
             * </ul>
             * This bundle was declared in catalog libs.versions.toml
             */
            public Provider<ExternalModuleDependencyBundle> getTesting() { return createBundle("kotlin.testing"); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for dokka to the plugin id 'org.jetbrains.dokka'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getDokka() { return createPlugin("dokka"); }

            /**
             * Creates a plugin provider for gitSemVer to the plugin id 'org.danilopianini.git-sensitive-semantic-versioning-gradle-plugin'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getGitSemVer() { return createPlugin("gitSemVer"); }

    }

}
