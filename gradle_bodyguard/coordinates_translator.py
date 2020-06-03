# coordinates_translator.py

def ossindex_to_maven(coordinate):
    return coordinate.replace('pkg:maven/', '').replace('/', ':').replace('@', ':')


def maven_to_ossindex(coordinate):
    return 'pkg:maven/{}/{}@{}'.format(*coordinate.split(':'))
