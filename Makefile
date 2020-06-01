clean:
	rm -rf *.egg-info
	rm -rf dist

install:
	poetry install

pytests:
	poetry run pytest -vv

build:
	poetry build

run:
	poetry run main