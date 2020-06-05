import json
from gradle_bodyguard.ossindex_fetcher import OSSIndexFetcher

FAKE_API = '/fake/api/component-report'
FAKE_TOKEN = 'fake-token'


def test_fetched_with_success_no_vulnerabilities(httpserver):

    # Given
    raw = '''
        [
            {
                "coordinates": "pkg:maven/com.squareup.okhttp3/okhttp@4.5.0",
                "vulnerabilities": []
            },
            {
                "coordinates": "pkg:maven/com.squareup.retrofit2/retrofit@2.7.0",
                "vulnerabilities": []
            }
        ]
    '''

    httpserver.expect_request(FAKE_API).respond_with_json(json.loads(raw))
    fetcher = OSSIndexFetcher(FAKE_TOKEN, httpserver.url_for(FAKE_API))

    # When
    found = fetcher.fetch([])

    # Then
    expected = {}
    assert found == expected


def test_fetched_with_success_found_vulnerabilities(httpserver):

    # Given
    raw = '''
        [
            {
                "coordinates": "pkg:maven/com.squareup.okhttp3/okhttp@3.1.2",
                "vulnerabilities": [
                    { "cve": "CVE-2018-20200" }
                ]
            },
            {
                "coordinates": "pkg:maven/com.squareup.retrofit2/retrofit@2.2.0",
                "vulnerabilities": []
            }
        ]
    '''

    httpserver.expect_request(FAKE_API).respond_with_json(json.loads(raw))
    fetcher = OSSIndexFetcher(FAKE_TOKEN, httpserver.url_for(FAKE_API))

    # When
    found = fetcher.fetch([])

    # Then
    expected = {
        'pkg:maven/com.squareup.okhttp3/okhttp@3.1.2':'CVE-2018-20200'
    }

    assert found == expected


def test_fetch_error_badrequest(httpserver):

    # Given
    raw = '''
        {
            "code": 400,
            "message": "Missing coordinates version"
        }
    '''

    httpserver.expect_request(FAKE_API).respond_with_json(json.loads(raw), status=400)
    fetcher = OSSIndexFetcher(FAKE_TOKEN, httpserver.url_for(FAKE_API))

    # When
    found = fetcher.fetch([])

    # Then
    expected = {}
    assert found == expected


def test_fetch_error_internal_server(httpserver):

    # Given
    raw = '''
        {
            "code": 503,
            "message": "Internal Server Error"
        }
    '''

    httpserver.expect_request(FAKE_API).respond_with_json(json.loads(raw), status=503)
    fetcher = OSSIndexFetcher(FAKE_TOKEN, httpserver.url_for(FAKE_API))

    # When
    found = fetcher.fetch([])

    # Then
    expected = {}
    assert found == expected
