from . import package_coordinates

def generate(vulnerabilities, ocurrences):
	
	report = {
		'has_issues': False,
		'issues':[]
	}
	
	if(vulnerabilities):
		report['has_issues'] = True

		for package, cve in sorted(vulnerabilities.items()):
			maven_coordinates = package_coordinates.ossindex_to_maven(package)
			issue = {
				'cve': nist_cve_url(cve),
				'dependency':maven_coordinates,
				'usage_samples': samples(ocurrences, maven_coordinates)
			}

			report['issues'].append(issue)

	return report

def nist_cve_url(cve):
    return f"https://nvd.nist.gov/vuln/detail/{cve}"

def samples(ocurrences, maven_coordinates):
	return ', '.join(sorted(list(ocurrences[maven_coordinates]))[:3])
