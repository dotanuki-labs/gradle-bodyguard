# cli_parser.py

import argparse


def parse(argv):
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
        '-f',
        '--force_exit',
        action='store',
        default=False,
        help='Force program to exit with failure when issues were found'
    )

    parsed = parser.parse_args(argv)
    ignored = [] if parsed.ignore == '' else parsed.ignore.split(',')
    return [parsed.project, parsed.destination, ignored, parsed.force_exit]
