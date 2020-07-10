from gradle_bodyguard import app

import json
import os

# Note : using tmpdir fixture from pytest
# https://docs.pytest.org/en/latest/tmpdir.html

FIXTURES_DIR = f"{os.getcwd()}/tests/fixtures"


def test_integration_against_javajwt(tmpdir):

    # Given
    target = f"{FIXTURES_DIR}/java-jwt@79ed2431"
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

    written = open(f"{tmpdir}/gradle-bodyguard-report.json").read()
    assert 'CVE-2018-20200' in written
