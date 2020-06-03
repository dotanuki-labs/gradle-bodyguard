# app.py

from . import cli_parser
from . import gradlew_locator
from . import gradle_scanner
from . import report_generator
from . import security_reporter
from . import vulnerabilities_matcher

from .gradle_runner import GradleTaskRunner
from .ossindex_fetcher import OSSIndexFetcher
from .utils.cli_colorizer import cyan

import sys


def main(argv=None):

    print(f"\n\n{cyan('GradleBodyguard (version 0.0.2)')}\n")

    (project, destination, ignore, force_exit) = cli_parser.parse(argv)

    print("Running with :\n")
    print(f"🤖 Target project → {project}")
    print(f"🤖 Reporting to → {destination}")
    print(f"🤖 Ignoring CVEs → {ignore if len(ignore) > 0 else 'None'}\n")

    gradlew = gradlew_locator.locate(project)

    print("🔥 Gradlew found at → {gradlew}")

    runner = GradleTaskRunner(gradlew)

    print("🔥 Start scanning Gradle project ...")

    (dependencies, ocurrences) = gradle_scanner.scan(runner)

    print(f"🔥 Total number of dependencies found → {len(dependencies)}")
    print("🔥 Matching against OSS Index ... ")
    fetcher = OSSIndexFetcher()
    vulnerabilities = vulnerabilities_matcher.match(dependencies, fetcher)

    print("🔥 Generating security report ... ")
    report = report_generator.generate(vulnerabilities, ocurrences, ignore)
    security_reporter.deliver(report, destination)
    print("\n🤖 Done\n")

    # Break CI pipeline if needed

    if force_exit and report['has_issues']:
        sys.exit(1)
