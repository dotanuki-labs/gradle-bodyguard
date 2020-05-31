from gradle_bodyguard.gradle_scanner import find_dependencies

def test_scanner():
    assert find_dependencies() == 'Hello World'