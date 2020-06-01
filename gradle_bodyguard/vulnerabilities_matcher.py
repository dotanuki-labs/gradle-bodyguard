from . import package_coordinates

CHUNK_SIZE_FOR_COORDINATES=100

def match(dependencies, fetcher, chunk_size=CHUNK_SIZE_FOR_COORDINATES):
    coordinates = [package_coordinates.maven_to_ossindex(artefact) for artefact in dependencies]
    chunks = list(chunkify(coordinates, chunk_size))
    vulnerabilities = {}

    for chunk in chunks:
    	for package, cve in fetcher.fetch(chunk).items():
    		vulnerabilities[package] = cve

    return vulnerabilities

# https://stackoverflow.com/a/2135920/1880882
def chunkify(items, size):
	minimum = min(size, len(items))
	if minimum == 0:
		return ([])

	k, m = divmod(len(items), minimum)
	return (items[i * k + min(i, m):(i + 1) * k + min(i + 1, m)] for i in range(size))