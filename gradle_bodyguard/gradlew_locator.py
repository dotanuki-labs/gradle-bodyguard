# gradlew_locator.py

import os
import sys


def locate(project):
    gradlew = f"{project}/gradlew"

    if os.path.isfile(gradlew):
        os.chdir(project)
        return gradlew
    else:
        print(f"💩 Error : gradlew script not found at {project}")
        sys.exit(1)
