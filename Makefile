clean:
	rm -rf *.egg-info
	rm -rf dist

install:
	poetry install

pytests:
	poetry run pytest

build:
	poetry build