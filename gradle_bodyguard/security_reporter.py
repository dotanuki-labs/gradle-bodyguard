# security_reporter.py

from .utils.cli_colorizer import blue
from .utils.cli_colorizer import cyan
from .utils.cli_colorizer import red

import json


def deliver(report, destination):
    if not report['has_issues']:
        print("\n✅ Awesome : no potential security issues found!")
    else:
        print("\n⛔️ Potential security issues found!")
        if destination == 'stdout':
            plain_text(report)
        else:
            write_json(destination, report)


def plain_text(report):
    for issue in report['issues']:
        print(f"\n{red(issue['cve'])} tracks a known security issue for {cyan(issue['dependency'])}")
        print(f"Some modules that consume this artefact → {cyan(issue['usage_samples'])}")
        print(f"Learn more about the vulnerability → {blue(issue['learn_more'])}")


def write_json(destination, report):
    with open(f"{destination}/gradle-bodyguard-report.json", "w") as writer:
        writer.write(json.dumps(report, indent=2, sort_keys=True))
        writer.close()

    print(f"📌 JSON report written at → {destination}/gradle-bodyguard-report.json")
