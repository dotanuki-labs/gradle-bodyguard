package com.google.android.gms;

import com.google.android.gms.dependencies.DependencyAnalyzer;
import com.google.android.gms.dependencies.DependencyInspector;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

/**
 * Attaches a listener to enforce Google Play services dependencies.
 *
 * @see DependencyInspector
 */
public class StrictVersionMatcherPlugin implements Plugin<Project> {
  /**
   * Static tracking of dependency information across all modules the plugin is applied to.
   */
  private static DependencyAnalyzer globalDependencies = new DependencyAnalyzer();

  @Override
  public void apply(@Nonnull Project project) {
    // When debugging and testing ensure to look at release dependencies,
    // not testing dependencies because of the Android test-app
    // de-duplication that happens to produce an Android test app that
    // can be run in the same process as the Android App (under test).
    project.getGradle().addListener(
        new DependencyInspector(globalDependencies, project.getName(),
            "This error message came from the strict-version-matcher-plugin Gradle plugin, report" +
                " issues at https://github.com/google/play-services-plugins and disable by " +
                "removing the reference to the plugin (\"apply 'strict-version-matcher-plugin'\")" +
                " from build.gradle."));
  }
}
