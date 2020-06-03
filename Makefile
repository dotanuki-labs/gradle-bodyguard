clean:
	rm -rf *.egg-info
	rm -rf dist
	rm -rf gradle_bodyguard/__pycache__
	rm -rf tests/__pycache__
	rm -rf .pytest_cache

install:
	poetry install

checkstyle:
	flake8 gradle_bodyguard tests

pytests:
	poetry run pytest -vv

help:
	poetry run gradle-bodyguard

run:
	poetry run gradle-bodyguard -p $(project)

config_pypi_token:
	poetry config pypi-token.pypi $(token)

publish:
	poetry build
	poetry publish
