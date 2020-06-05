from . import coordinates_translator
from .utils import dependencies_slicer

CHUNK_SIZE_FOR_COORDINATES = 125


def match(dependencies, fetcher, chunk_size=CHUNK_SIZE_FOR_COORDINATES):
    coordinates = [coordinates_translator.maven_to_ossindex(artefact) for artefact in dependencies]
    chunks = dependencies_slicer.slice(coordinates, chunk_size)

    vulnerabilities = {}

    for chunk in chunks:
        for package, cve in fetcher.fetch(chunk).items():
            vulnerabilities[package] = cve

    return vulnerabilities
