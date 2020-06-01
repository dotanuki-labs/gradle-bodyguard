from gradle_bodyguard.vulnerabilities_matcher import match_vulnerabilities
from .fakes import FakeOSSIndexFetcher

def test_match_empty_dependencies():

	# Given
	dependencies = {}
	fetcher = FakeOSSIndexFetcher(dependencies)

	# When
	vulnerabilities = match_vulnerabilities(dependencies, fetcher)

	# Then
	assert len(vulnerabilities) == 0

def test_match_dependencies_when_fit_chunk_size():

	# Given
	dependencies = {
		'com.squareup.okhttp3:okhttp:3.1.2',
		'com.squareup.retrofit2:retrofit:2.2.0'
	}

	matched = {
		'pkg:maven/com.squareup.okhttp3/okhttp@3.1.2':'CVE-2018-20200'
	}

	fetcher = FakeOSSIndexFetcher(matched)

	# When
	vulnerabilities = match_vulnerabilities(dependencies, fetcher)

	# Then
	assert vulnerabilities == matched

def test_match_dependencies_force_chunk_spliting():

	# Given
	dependencies = {
		'com.squareup.okhttp3:okhttp:3.1.2',
		'com.squareup.okhttp3:okhttp-mockwebserver:3.1.2',
		'com.squareup.okhttp3:okhttp-httplogger:3.1.2',
		'com.squareup.retrofit2:retrofit:2.2.0',
		'com.squareup.retrofit2:retrofit-converters:2.2.0'
	}

	matched = {
		'pkg:maven/com.squareup.okhttp3/okhttp@3.1.2':'CVE-2018-20200'
	}

	fetcher = FakeOSSIndexFetcher(matched)

	# When
	vulnerabilities = match_vulnerabilities(dependencies, fetcher, chunk_size=2)

	# Then
	assert vulnerabilities == matched