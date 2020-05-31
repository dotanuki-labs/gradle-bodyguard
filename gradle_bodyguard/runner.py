from . import gradle_scanner

def main(argv=None):
	dependencies = gradle_scanner.find_dependencies()
	print(dependencies)