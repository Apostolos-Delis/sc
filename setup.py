#!/usr/bin/env python3
# coding: utf8


import pathlib

from setuptools import find_packages, setup

from shorts import __VERSION__

# Get the long description from the README file
current_dir = pathlib.Path(__file__).parent.resolve()
long_description = (current_dir / 'README.md').read_text(encoding='utf-8')

setup(
    name="shorts",
    version=__VERSION__,
    packages=find_packages(),
    scripts=["bin/sc"],
    # Project uses reStructuredText, so ensure that the docutils get
    # installed or upgraded on the target machine
    install_requires=["PyYAML>=5.4.1"],
    # metadata for upload to PyPI
    author="Apostolos Delis",
    author_email="apost.delis@gmail.com",
    description="A CLI Shortcut Managment tool",
    license="GNU General Public License",
    keywords=["shortcuts", "shorts", "alias", "cli"],
    url="https://github.com/Apostolos-Delis/shorts",
    # could also include long_description, download_url, classifiers, etc.
    # For Linting Purposes
    setup_requires=["flake8"],
    # Tests
    tests_require=["pytest"],
)


if __name__ == "__main__":
    pass
