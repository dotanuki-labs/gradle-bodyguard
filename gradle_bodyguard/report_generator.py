from . import coordinates_translator


def generate(vulnerabilities, ocurrences, must_ignore=[]):

    report = {
        'has_issues': False,
        'issues':[]
    }

    if len(vulnerabilities) != 0:
        for package, cve in sorted(vulnerabilities.items()):
            if cve not in must_ignore:
                maven_coordinates = coordinates_translator.ossindex_to_maven(package)
                issue = {
                    'cve': cve,
                    'dependency':maven_coordinates,
                    'usage_samples': samples(ocurrences, maven_coordinates),
                    'learn_more': nist_url(cve),
                }

                report['issues'].append(issue)

        if len(report['issues']) != 0:
            report['has_issues'] = True

    return report


def nist_url(cve):
    return f"https://nvd.nist.gov/vuln/detail/{cve}"


def samples(ocurrences, maven_coordinates):
    return ', '.join(sorted(list(ocurrences[maven_coordinates]))[:3])
