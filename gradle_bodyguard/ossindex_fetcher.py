import requests

OSS_INDEX_URL = 'https://ossindex.sonatype.org/api/v3/component-report'
DEFAULT_SECONDS_TO_TIMEOUT = 10


class OSSIndexFetcher:
    def __init__(self, url=OSS_INDEX_URL, timeout=DEFAULT_SECONDS_TO_TIMEOUT):
        self.url = url

    def fetch(self, coordinates):
        founded = {}

        response = requests.post(self.url, json={'coordinates': coordinates})

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
        print(f"Http Status -> {response.status_code}")
