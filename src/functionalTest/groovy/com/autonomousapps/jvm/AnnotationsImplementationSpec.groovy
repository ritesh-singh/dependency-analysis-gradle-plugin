package com.autonomousapps.jvm

import com.autonomousapps.jvm.projects.AnnotationsCompileOnlyProject
import com.autonomousapps.jvm.projects.AnnotationsImplementationProject
import com.autonomousapps.jvm.projects.AnnotationsImplementationProject2
import com.autonomousapps.utils.Colors

import static com.autonomousapps.utils.Runner.build
import static com.google.common.truth.Truth.assertThat

final class AnnotationsImplementationSpec extends AbstractJvmSpec {

  def "classes used in runtime-retained annotations are implementation (#gradleVersion)"() {
    given:
    def project = new AnnotationsImplementationProject()
    gradleProject = project.gradleProject

    when:
    build(gradleVersion, gradleProject.rootDir, 'buildHealth')

    then:
    assertThat(project.actualBuildHealth()).containsExactlyElementsIn(project.expectedBuildHealth)

    where:
    gradleVersion << gradleVersions()
  }

  // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1290
  def "runtime-retained annotations are implementation (#gradleVersion)"() {
    given:
    def project = new AnnotationsImplementationProject2()
    gradleProject = project.gradleProject

    when:
    build(gradleVersion, gradleProject.rootDir, 'buildHealth')

    then:
    assertThat(project.actualBuildHealth()).containsExactlyElementsIn(project.expectedBuildHealth)

    when:
    def result = build(gradleVersion, gradleProject.rootDir, 'consumer:reason', '--id', 'org.cthing:cthing-annotations')

    then:
    assertThat(Colors.decolorize(result.output)).contains(
      '''\
        ------------------------------------------------------------
        You asked about the dependency 'org.cthing:cthing-annotations:1.0.0'.
        There is no advice regarding this dependency.
        ------------------------------------------------------------
        
        Shortest path from :consumer to org.cthing:cthing-annotations:1.0.0 for compileClasspath:
        :consumer
        \\--- org.cthing:cthing-annotations:1.0.0
        
        Shortest path from :consumer to org.cthing:cthing-annotations:1.0.0 for runtimeClasspath:
        :consumer
        \\--- org.cthing:cthing-annotations:1.0.0
        
        Shortest path from :consumer to org.cthing:cthing-annotations:1.0.0 for testCompileClasspath:
        :consumer
        \\--- org.cthing:cthing-annotations:1.0.0
        
        Shortest path from :consumer to org.cthing:cthing-annotations:1.0.0 for testRuntimeClasspath:
        :consumer
        \\--- org.cthing:cthing-annotations:1.0.0
        
        Source: main
        ------------
        * Uses (in an annotation) 1 class: org.cthing.annotations.PackageNonnullByDefault (implies implementation, sometimes).
        
        Source: test
        ------------
        (no usages)'''.stripIndent()
    )

    where:
    gradleVersion << gradleVersions()
  }

  def "classes used in compile-retained annotations are compileOnly (#gradleVersion)"() {
    given:
    def project = new AnnotationsCompileOnlyProject()
    gradleProject = project.gradleProject

    when:
    build(gradleVersion, gradleProject.rootDir, 'buildHealth')
    // TODO(tsr): still need better tests for reason. Before the fix, this output was wrong. Still not fixed really.
    //, ':consumer:reason', '--id', 'org.jetbrains:annotations')

    then:
    assertThat(project.actualBuildHealth()).containsExactlyElementsIn(project.expectedBuildHealth)

    where:
    gradleVersion << gradleVersions()
  }
}
