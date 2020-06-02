from gradle_bodyguard import app

import json
import os

# Note : using tmpdir fixture from pytest
# https://docs.pytest.org/en/latest/tmpdir.html

FIXTURES_DIR=f"{os.getcwd()}/tests/fixtures"

def test_integration_against_rxjava(tmpdir):

	target = f"{FIXTURES_DIR}/rxjava@d3dd133c"
	argv = ['-p', target, '-d', f"{tmpdir}"]

	# When 
	app.main(argv)

	# Then
	expected = {
		'has_issues': True,
 		'issues': [
			{
				'cve': 'CVE-2019-10782',
				'dependency': 'com.puppycrawl.tools:checkstyle:8.26',
				'learn_more': 'https://nvd.nist.gov/vuln/detail/CVE-2019-10782',
				'usage_samples': ':'
			}
		]
	}
	
	written = open(f"{tmpdir}/gradle-bodyguard-report.json")
	reported = json.load(written)
	assert expected == reported

	
def test_integration_against_play_services_plugin(tmpdir):

	# Given
	target = f"{FIXTURES_DIR}/gms-plugins@1baeac2a/google-services-plugin"
	argv = ['-p', target, '-d', f"{tmpdir}"]

	# When 
	app.main(argv)

	# Then
	# No file is written when no CVEs found
	absent = f"{tmpdir}/gradle-bodyguard-report.json"
	assert not os.path.exists(absent)

def test_integration_against_plaid(tmpdir):

	# Given
	target = f"{FIXTURES_DIR}/plaid@e703957b"
	ignored_cves = 'CVE-2018-10237,CVE-2018-1324,CVE-2017-13098'
	argv = ['-p', target, '-d', f"{tmpdir}", '-i', ignored_cves]

	# When 
	app.main(argv)

	# Then
	# We ignored all other CVEs other than OkHttp
	expected = {
		'has_issues': True,
 		'issues': [
			{
				'cve': 'CVE-2018-20200',
				'dependency': 'com.squareup.okhttp3:okhttp:3.12.0',
				'learn_more': 'https://nvd.nist.gov/vuln/detail/CVE-2018-20200',
				'usage_samples': ':about, :app, :core'
			}
		]
	}
	
	written = open(f"{tmpdir}/gradle-bodyguard-report.json")
	reported = json.load(written)
	assert expected == reported