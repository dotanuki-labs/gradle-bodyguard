import requests

from . import constants


OSS_INDEX_URL = 'https://ossindex.sonatype.org/api/v3/component-report'
DEFAULT_SECONDS_TO_TIMEOUT = 10


class OSSIndexFetcher:
    def __init__(self, api_token, url=OSS_INDEX_URL, timeout=DEFAULT_SECONDS_TO_TIMEOUT):
        self.api_token = api_token
        self.url = url

    def fetch(self, coordinates):
        founded = {}
        extra_headers = {'user-agent': f"x-gradle-bodyguard/{constants.APP_VERSION}"}

        if self.api_token:
            extra_headers['authorization'] = f"Basic {self.api_token}"

        response = requests.post(self.url, json={'coordinates': coordinates}, headers=extra_headers)

        if response.status_code == requests.codes.ok:
            for item in response.json():
                for vulnerability in item['vulnerabilities']:
                    if 'cve' in vulnerability:  # For some reason is missing in sometime ...
                        founded[item['coordinates']] = vulnerability['cve']
        else:
            self.report_http_error(response)

        return founded

    def report_http_error(self, response):
        print('Error when fetching from OSS Index')
        print(f"{response.text}")
