# app.py

from . import cli_parser
from . import gradlew_locator
from . import gradle_scanner
from . import report_generator
from . import security_reporter
from . import vulnerabilities_matcher

from .gradle_runner import GradleTaskRunner
from .ossindex_fetcher import OSSIndexFetcher

import sys
import textwrap

def main(argv=None):

	print(prompt())	

	(project, destination, ignore, force_exit) = cli_parser.parse(argv)

	print("Running with :\n")	
	print(f"🤖 Target project → {project}")
	print(f"🤖 Reporting to → {destination}")
	print(f"🤖 Ignoring CVEs → {ignore if len(ignore) > 0 else 'None'}\n")

	gradlew = gradlew_locator.locate(project)

	print(f"🔥 Gradlew found at → {gradlew}")

	runner = GradleTaskRunner(gradlew)

	print(f"🔥 Start scanning Gradle project ...")
	
	(dependencies, ocurrences) = gradle_scanner.scan(runner)

	print(f"🔥 Total number of dependencies found → {len(dependencies)}")
	print(f"🔥 Matching against OSS Index ... ")
	vulnerabilities = vulnerabilities_matcher.match(dependencies, OSSIndexFetcher())

	print(f"🔥 Generating security report ... ")
	report = report_generator.generate(vulnerabilities, ocurrences, ignore)
	security_reporter.deliver(report, destination)

	print(f"\n🤖 Done\n")
	
	# Break CI pipeline if needed
	if force_exit:
		sys.exit(1) 

def prompt():
	logo='''
	   ______               ____        ____            __                                 __
	  / ____/________ _____/ / /__     / __ )____  ____/ /_  ______ ___  ______ __________/ /
	 / / __/ ___/ __ `/ __  / / _ \\   / __  / __ \\/ __  / / / / __ `/ / / / __ `/ ___/ __  / 
	/ /_/ / /  / /_/ / /_/ / /  __/  / /_/ / /_/ / /_/ / /_/ / /_/ / /_/ / /_/ / /  / /_/ /  
	\\____/_/  \\__,_/\\__,_/_/ \\___/  /_____/\\____/\\__,_/\\__, /\\__, /\\__,_/\\__,_/_/   \\__,_/   
	                                                  /____//____/                           
	'''
	return textwrap.dedent(logo)
