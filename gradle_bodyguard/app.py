# app.py

from . import cli_parser
from . import gradlew_locator
from . import gradle_scanner
from . import report_generator
from . import vulnerabilities_matcher

from .gradle_runner import GradleTaskRunner
from .ossindex_fetcher import OSSIndexFetcher

def main(argv):
	(project, destination, report) = cli_parser.parse(argv)
	
	print(f"Project -> {project}")
	print(f"Report -> {report}")
	print(f"Destination -> {destination}")
	
	gradlew = gradlew_locator.locate(project)
	runner = GradleTaskRunner(gradlew)
	
	(dependencies, ocurrences) = gradle_scanner.scan(runner)
	vulnerabilities = vulnerabilities_matcher.match(dependencies, OSSIndexFetcher())
	security_report = report_generator.generate(vulnerabilities, ocurrences)

	if [security_report['has_issues']]:
		print("Security issues found!")
		print(security_report['issues'])
	else:
		print("No security issues found!")
