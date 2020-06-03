from gradle_bodyguard.utils import dependencies_slicer


def test_slicing_empty_list():

    # Given
    target = []
    chunck_size = 2

    # When
    sliced = dependencies_slicer.slice(target, chunck_size)

    # Then
    assert len(sliced) == 0


def test_slicing_single_chunk():

    # Given
    target = [1, 2, 3, 4, 5]
    chunck_size = 5

    # When
    sliced = dependencies_slicer.slice(target, chunck_size)

    # Then
    expected = [[1, 2, 3, 4, 5]]
    assert sliced == expected


def test_slicing_fit_to_chunk():

    # Given
    target = [1, 2, 3, 4, 5]
    chunck_size = 3

    # When
    sliced = dependencies_slicer.slice(target, chunck_size)

    # Then
    expected = [[1, 2, 3], [4, 5]]
    assert sliced == expected


def test_slicing_chunk_size_overflow():

    # Given
    target = [1, 2, 3, 4, 5]
    chunck_size = 6

    # When
    sliced = dependencies_slicer.slice(target, chunck_size)

    # Then
    expected = [[1, 2, 3, 4, 5]]
    assert sliced == expected


def test_slicing_several_chunks():

    # Given
    target = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    chunck_size = 4

    # When
    sliced = dependencies_slicer.slice(target, chunck_size)

    # Then
    expected = [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10]]
    assert sliced == expected
