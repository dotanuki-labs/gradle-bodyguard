import json

from gradle_bodyguard import report_generator

def test_no_vulnerabilities():
	
	# Given
	vulnerabilities = {}

	ocurrences = {
		'com.squareup.okhttp3:okhttp:3.1.2':{':'},
		'com.squareup.retrofit2:retrofit:2.2.0':{':'}
	}

	# When
	report = report_generator.generate(vulnerabilities, ocurrences)

	# Then
	expected = {
		'has_issues': False,
		'issues':[]
	}

	assert report == expected

def test_report_generation_unique_occurence_on_modules():

	vulnerabilities = {
		'pkg:maven/com.squareup.okhttp3/okhttp@3.1.2':'CVE-2018-20200'
	}

	ocurrences = {
		'com.squareup.okhttp3:okhttp:3.1.2':{':'},
		'com.squareup.retrofit2:retrofit:2.2.0':{':'}
	}

	# When
	report = report_generator.generate(vulnerabilities, ocurrences)

	# Then
	expected = {
		'has_issues': True,
		'issues':[
			{
				'cve':'CVE-2018-20200',
				'dependency':'com.squareup.okhttp3:okhttp:3.1.2',
				'usage_samples':':',
				'learn_more':'https://nvd.nist.gov/vuln/detail/CVE-2018-20200',
			}
		]
	}

	assert report == expected

def test_report_generation_multiple_occurence_on_modules():

	vulnerabilities = {
		'pkg:maven/com.squareup.okhttp3/okhttp@3.1.2':'CVE-2018-20200'
	}

	ocurrences = {
		'com.squareup.okhttp3:okhttp:3.1.2':{':core',':deeplinks'},
		'com.squareup.retrofit2:retrofit:2.2.0':{':core'}
	}

	# When
	report = report_generator.generate(vulnerabilities, ocurrences)

	# Then
	expected = {
		'has_issues': True,
		'issues':[
			{
				'cve':'CVE-2018-20200',
				'dependency':'com.squareup.okhttp3:okhttp:3.1.2',
				'usage_samples': ':core, :deeplinks',
				'learn_more':'https://nvd.nist.gov/vuln/detail/CVE-2018-20200',
			}
		]
	}

	assert report == expected