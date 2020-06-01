# app.py

from . import cli_parser
from . import gradlew_locator
from . import gradle_scanner
from . import report_generator
from . import vulnerabilities_matcher

from .gradle_runner import GradleTaskRunner
from .ossindex_fetcher import OSSIndexFetcher

import textwrap

def main(argv=None):
	(project, destination, report) = cli_parser.parse(argv)

	print(prompt())	
	print("Running with :\n")	
	print(f"🤖 Project →  {project}")
	print(f"🤖 Report Style → {report}")
	print(f"🤖 Destination → {destination}\n")
	
	gradlew = gradlew_locator.locate(project)

	print(f"🔥 Gradlew found at → {gradlew}")

	runner = GradleTaskRunner(gradlew)

	print(f"🔥 Starting Gradle project scan ...")
	
	(dependencies, ocurrences) = gradle_scanner.scan(runner)

	print(f"🔥 Total number of dependencies found → {len(dependencies)}")
	print(f"🔥 Matching against OSS Index ... ")
	
	vulnerabilities = vulnerabilities_matcher.match(dependencies, OSSIndexFetcher())
	report = report_generator.generate(vulnerabilities, ocurrences)

	if report['has_issues']:
		print("\n☠️ Potential security issues found!")
		print(report['issues'])
	else:
		print("\n🚀 Awesome : no potential security issues found!")

def prompt():
	logo='''
	   ______               ____        ____            __                                 __
	  / ____/________ _____/ / /__     / __ )____  ____/ /_  ______ ___  ______ __________/ /
	 / / __/ ___/ __ `/ __  / / _ \\   / __  / __ \\/ __  / / / / __ `/ / / / __ `/ ___/ __  / 
	/ /_/ / /  / /_/ / /_/ / /  __/  / /_/ / /_/ / /_/ / /_/ / /_/ / /_/ / /_/ / /  / /_/ /  
	\\____/_/  \\__,_/\\__,_/_/\\___/  /_____/\\____/\\__,_/\\__, /\\__, /\\__,_/\\__,_/_/   \\__,_/   
	                                                  /____//____/                           
	'''
	return textwrap.dedent(logo)
