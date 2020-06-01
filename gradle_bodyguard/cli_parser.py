# cli_parser.py

import argparse

def parse(argv):
	parser = argparse.ArgumentParser(
		prog='Gradle Bodyguard', 
		description='Scans a Gradle project dependencies and warns about potential security issues'
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

	parsed = parser.parse_args(argv)
	return [parsed.project, parsed.destination]