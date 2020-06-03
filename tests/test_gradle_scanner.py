from gradle_bodyguard import gradle_scanner
from .fakes import FakeGradleRunner


def test_project_without_dependencies():

    # Given
    projects = '''
        > Task :projects

        ------------------------------------------------------------
        Root project
        ------------------------------------------------------------

        Root project 'test'
        No sub-projects
        '''

    modules = {
        'dependencies':'''
            > Task :dependencies

            ------------------------------------------------------------
            Root project
            ------------------------------------------------------------

            compileClasspath - Compile classpath for source set 'main'.
            No dependencies

            ...

            testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
            No dependencies
            '''
    }

    runner = FakeGradleRunner(projects, modules)

    # When
    (dependencies, ocurrences) = gradle_scanner.scan(runner)

    # Then
    assert len(dependencies) == 0
    assert len(ocurrences) == 0


def test_root_module_only_with_distinct_ocurrences():

    # Given
    projects = '''
        > Task :projects

        Root project 'fake'
        No sub-projects
        '''

    modules = {
        'dependencies': '''
            +--- com.squareup.okhttp3:okhttp:4.5.0 (n)
            \\--- com.squareup.retrofit2:retrofit:2.6.0 (n)
        '''
    }

    runner = FakeGradleRunner(projects, modules)

    # When
    (dependencies, ocurrences) = gradle_scanner.scan(runner)

    # Then
    expected_dependencies = {
        'com.squareup.okhttp3:okhttp:4.5.0',
        'com.squareup.retrofit2:retrofit:2.6.0'
    }

    expected_ocurrences = {
        'com.squareup.okhttp3:okhttp:4.5.0':{':'},
        'com.squareup.retrofit2:retrofit:2.6.0':{':'}
    }

    assert dependencies == expected_dependencies
    assert ocurrences == expected_ocurrences


def test_multiple_modules_distinct_ocurrences():

    # Given
    projects = '''
        > Task :projects

        Root project 'fake'
        +--- Project ':app'
        \\--- Project ':core'
        '''

    modules = {
        'dependencies':'',
        ':app:dependencies': '''
            +--- com.squareup.okhttp3:okhttp:4.5.0 (n)
            \\--- com.squareup.retrofit2:retrofit:2.6.0 (n)
        ''',
        ':core:dependencies': '''
            +--- junit:junit:4.12 (n)
            \\--- org.mockito:mockito-core:2.23.0 (n)
        '''
    }

    runner = FakeGradleRunner(projects, modules)

    # When
    (dependencies, ocurrences) = gradle_scanner.scan(runner)

    # Then
    expected_dependencies = {
        'com.squareup.okhttp3:okhttp:4.5.0',
        'com.squareup.retrofit2:retrofit:2.6.0',
        'junit:junit:4.12',
        'org.mockito:mockito-core:2.23.0'
    }

    expected_ocurrences = {
        'com.squareup.okhttp3:okhttp:4.5.0':{':app'},
        'com.squareup.retrofit2:retrofit:2.6.0':{':app'},
        'junit:junit:4.12':{':core'},
        'org.mockito:mockito-core:2.23.0':{':core'},
    }

    assert dependencies == expected_dependencies
    assert ocurrences == expected_ocurrences


def test_multiple_modules_repeated_ocurrences():

    # Given
    projects = '''
        > Task :projects

        Root project 'fake'
        +--- Project ':app'
        \\--- Project ':core'
    '''

    modules = {
        'dependencies':'',
        ':app:dependencies': '''
            +--- com.squareup.okhttp3:okhttp:4.5.0 (n)
            \\--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72 (n)
        ''',
        ':core:dependencies': '''
        \\--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72 (n)
        '''
    }

    runner = FakeGradleRunner(projects, modules)

    # When
    (dependencies, ocurrences) = gradle_scanner.scan(runner)

    # Then
    expected_dependencies = {
        'com.squareup.okhttp3:okhttp:4.5.0',
        'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72'
    }

    expected_ocurrences = {
        'com.squareup.okhttp3:okhttp:4.5.0':{':app'},
        'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72':{':app',':core'}
    }

    assert dependencies == expected_dependencies
    assert ocurrences == expected_ocurrences
