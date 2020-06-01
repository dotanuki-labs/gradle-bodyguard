
class FakeGradleRunner:
	def __init__(self, projects, dependencies):
		self.projects = projects
		self.dependencies = dependencies

	def execute(self, task):
		print(task)
		if task == ':projects':
			return self.projects

		return self.dependencies[task]

class FakeOSSIndexFetcher:
	def __init__(self, found):
		self.found = found

	def fetch(self, coordinates):
		return self.found

		