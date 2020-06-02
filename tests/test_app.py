from gradle_bodyguard import app

import json
import os

# Note : using tmpdir fixture from pytest
# https://docs.pytest.org/en/latest/tmpdir.html

def test_integration_against_rxjava(tmpdir):

	# Given
	clone_url = 'git@github.com:ReactiveX/RxJava.git'
	revision = 'd3dd133c3fe30610c3f4fa878de8285bf95177de'

	os.chdir(tmpdir)
	os.system(f"git clone {clone_url}")
	os.system(f"git checkout {revision}")

	argv = ['-p', f"{tmpdir}/RxJava", '-d', f"{tmpdir}"]

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
	clone_url = 'git@github.com:google/play-services-plugins.git'
	revision = '1baeac2ab76fd6c734c9d9ef545c405e85d6e262'

	os.chdir(tmpdir)
	os.system(f"git clone {clone_url}")
	os.system(f"git checkout {revision}")

	workdir = f"{tmpdir}/play-services-plugins/google-services-plugin"
	reportdir = f"{tmpdir}/play-services-plugins"
	
	argv = ['-p', workdir, '-d', reportdir]

	# When 
	app.main(argv)

	# Then
	# No file is written when no CVEs found
	absent = f"{reportdir}/gradle-bodyguard-report.json"
	assert not os.path.exists(absent)

def test_integration_against_plaid(tmpdir):

	# Given
	clone_url = 'git@github.com:android/plaid.git'
	revision = 'e703957b5e5d4728dea94f11f8d0d27d227f9725'

	os.chdir(tmpdir)
	os.system(f"git clone {clone_url}")
	os.system(f"git checkout {revision}")

	ignored_cves = 'CVE-2018-10237,CVE-2018-1324,CVE-2017-13098'
	argv = ['-p', f"{tmpdir}/plaid", '-d', f"{tmpdir}", '-i', ignored_cves]

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