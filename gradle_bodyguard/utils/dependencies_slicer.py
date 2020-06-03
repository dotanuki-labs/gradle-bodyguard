# dependencies_slicer.py

def slice(target, chunk_size):
    sliced = []
    start = 0
    end = len(target)
    done = False

    while not done and end - start != 0:
        if end - start < chunk_size:
            sliced.append(target[start:end])
            done = True
        else:
            cutpoint = start + chunk_size
            sliced.append(target[start:cutpoint])
            start = cutpoint

    return sliced
