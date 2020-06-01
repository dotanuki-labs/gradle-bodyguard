
def ossindex_to_maven(ossindex_coordinate):
	return ossindex_coordinate.replace("pkg:maven/", "").replace("/", ":").replace("@", ":") 

def maven_to_ossindex(maven_coordinate):
	return "pkg:maven/{}/{}@{}".format(*maven_coordinate.split(":"))