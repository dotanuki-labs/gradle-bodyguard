# gradle_scanner.py

import re


def scan(gradle_runner, logger):
    modules = compute_gradle_modules(gradle_runner)
    logger.log(f"🔥 Total number of Gradle modules → {len(modules)}")
    return evaluate_dependencies(gradle_runner, modules, logger)


def compute_gradle_modules(gradle_runner):
    raw_output = gradle_runner.execute(':projects')
    matches = re.findall(r'Project\s\':[\w-]+\'', raw_output)
    modules = [line.replace('Project ','').replace('\'','') for line in matches]
    modules.insert(0, ':')  # Don't forget root project
    return modules


def evaluate_dependencies(gradle_runner, modules, logger):
    ocurrences = {}
    dependencies = set()

    for module in modules:
        logger.log(f"🔥 Evaluating dependencies for module → {module}")
        for dependency in dependencies_per_module(gradle_runner, module):
            dependencies.add(dependency)
            if not ocurrences.get(dependency):
                ocurrences[dependency] = set()
            ocurrences[dependency].add(module)

    return [dependencies, ocurrences]


def dependencies_per_module(gradle_runner, module):
    dependencies_task = 'dependencies' if module == ':' else f"{module}:dependencies"
    raw_output = gradle_runner.execute(dependencies_task)
    return re.findall(r'[\w\.-]+:[\w\.-]+:[\d\.]+\d', raw_output)
