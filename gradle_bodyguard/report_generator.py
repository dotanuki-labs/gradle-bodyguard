from . import coordinates_translator

def generate(vulnerabilities, ocurrences):
	
	report = {
		'has_issues': False,
		'issues':[]
	}
	
	if len(vulnerabilities) != 0:
		report['has_issues'] = True

		for package, cve in sorted(vulnerabilities.items()):
			maven_coordinates = coordinates_translator.ossindex_to_maven(package)
			issue = {
				'cve': cve,
				'dependency':maven_coordinates,
				'usage_samples': samples(ocurrences, maven_coordinates),
				'learn_more': nist_url(cve),
			}

			report['issues'].append(issue)

	return report

def nist_url(cve):
    return f"https://nvd.nist.gov/vuln/detail/{cve}"

def samples(ocurrences, maven_coordinates):
	return ', '.join(sorted(list(ocurrences[maven_coordinates]))[:3])
