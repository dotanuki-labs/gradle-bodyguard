# cli_parser.py

import argparse
import sys

from .utils.cli_colorizer import blue


def parse(args):
    parser = argparse.ArgumentParser(
        prog='Gradle Bodyguard',
        description='Scans a Gradle project and warns about potential security issues'
    )

    parser.add_argument(
        '-p',
        '--project',
        action='store',
        required=True,
        help='Path to the target Gradle project you want to scan'
    )

    parser.add_argument(
        '-d',
        '--destination',
        action='store',
        default='stdout',
        help='Directory where you want the security report save'
    )

    parser.add_argument(
        '-i',
        '--ignore',
        action='store',
        default='',
        help='CVEs to ignore (separed with commas)'
    )

    parser.add_argument(
        '-t',
        '--token',
        action='store',
        default='',
        help='API token for OSS Index'
    )

    parser.add_argument(
        '-f',
        '--force_exit',
        action='store',
        default=False,
        help='Force program to exit with failure when issues were found'
    )

    parser.add_argument(
        '-s',
        '--silent',
        action='store',
        default=False,
        help='Expose more execution logs'
    )

    try:
        parsed = parser.parse_args(args)
        ignored = [] if parsed.ignore == '' else parsed.ignore.split(',')
        return [
            parsed.project,
            parsed.destination,
            ignored,
            parsed.token,
            parsed.force_exit,
            parsed.silent
        ]
    except:
        print(f"\nLearn more about with {blue('gradle-bodyguard --help')}")
        sys.exit(0)
