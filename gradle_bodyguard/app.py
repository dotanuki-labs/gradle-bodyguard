# app.py

from . import cli_parser
from . import constants
from . import gradlew_locator
from . import gradle_scanner
from . import report_generator
from . import security_reporter
from . import vulnerabilities_matcher

from .gradle_runner import GradleTaskRunner
from .ossindex_fetcher import OSSIndexFetcher
from .utils.logging import Logger
from .utils.cli_colorizer import cyan

import sys


def main(argv=None):

    logo = f"GradleBodyguard(version {constants.APP_VERSION})"
    print(f"\n\n{cyan(logo)}\n")

    (project, destination, ignore, api_token, force_exit, silent) = cli_parser.parse(argv)

    logger = Logger(silent)

    logger.log("Running with :\n")
    print(f"🤖 Target project → {project}")
    logger.log(f"🤖 Reporting to → {destination}")
    logger.log(f"🤖 Ignoring CVEs → {ignore if len(ignore) > 0 else 'None'}\n")

    gradlew = gradlew_locator.locate(project)

    logger.log(f"🔥 Gradlew found at → {gradlew}")

    runner = GradleTaskRunner(gradlew)

    logger.log("🔥 Start scanning Gradle project ...")

    (dependencies, ocurrences) = gradle_scanner.scan(runner, logger)

    logger.log(f"🔥 Total number of dependencies found → {len(dependencies)}")
    logger.log("🔥 Matching against OSS Index ... ")
    fetcher = OSSIndexFetcher(api_token)
    vulnerabilities = vulnerabilities_matcher.match(dependencies, fetcher)

    logger.log("🔥 Generating security report ... ")
    report = report_generator.generate(vulnerabilities, ocurrences, ignore)
    security_reporter.deliver(report, destination)
    print("\n🤖 Done\n")

    # Break CI pipeline if needed

    if force_exit and report['has_issues']:
        sys.exit(1)
