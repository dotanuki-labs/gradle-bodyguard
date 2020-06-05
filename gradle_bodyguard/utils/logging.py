# logger.py

class Logger:
    def __init__(self, silent=False):
        self.silent = silent

    def log(self, message):
        if not self.silent:
            print(message)
