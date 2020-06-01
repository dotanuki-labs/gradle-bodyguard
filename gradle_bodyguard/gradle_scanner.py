# gradle_scanner.py

import re

def find_dependencies(gradle_runner):
	modules = compute_gradle_modules(gradle_runner)
	return evaluate_dependencies(gradle_runner, modules)

def compute_gradle_modules(gradle_runner):
	rawOutput = gradle_runner.execute(':projects')
	matches = re.findall(r'Project\s\':[\w-]+\'', rawOutput)
	modules = [line.replace('Project ','').replace('\'','') for line in matches]
	modules.insert(0, ':') # Don't forget root project
	return modules

def evaluate_dependencies(gradle_runner, modules):
	ocurrences = {}
	dependencies = set()

	for module in modules:
		for dependency in dependencies_per_module(gradle_runner, module):
			dependencies.add(dependency)
			if not ocurrences.get(dependency):
				ocurrences[dependency] = set()
			ocurrences[dependency].add(module)
	
	return [dependencies, ocurrences]

def dependencies_per_module(gradle_runner, module):
	dependencies_task = 'dependencies' if module == ':' else f"{module}:dependencies"
	rawOutput = gradle_runner.execute(dependencies_task)
	return re.findall(r'[\w\.-]+:[\w\.-]+:[\d\.]+\d', rawOutput)
