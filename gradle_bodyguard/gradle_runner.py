# gradle_runner.py

import subprocess


class GradleTaskRunner:
    def __init__(self, gradlew):
        self.gradlew = gradlew

    def execute(self, task):
        params = [self.gradlew, task]
        child_process = subprocess.Popen(params, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        stdout, stderr = child_process.communicate()
        return f"{stdout}"
