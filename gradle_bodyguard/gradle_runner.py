import subprocess

class GradleTaskRunner(gradlew):
	def __init__(self, gradlew):
		self.gradlew = gradlew
		
	def execute(self, task):
		params = [gradlew, task]
		child_process = subprocess.Popen(params, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
		stdout, stderr = child_process.communicate()
		return f"{stdout}"