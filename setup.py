#!/usr/bin/env python3
# coding: utf8

from setuptools import setup, find_packages

from shortcut import __VERSION__

setup(
    name="shortcut",
    version=__VERSION__,
    packages=find_packages(),
    scripts=['bin/sc'],

    # Project uses reStructuredText, so ensure that the docutils get
    # installed or upgraded on the target machine
    install_requires=['PyYAML>=5.4.1'],

    # metadata for upload to PyPI
    author="Apostolos Delis",
    author_email="apost.delis@gmail.com",
    description="A CLI Shortcut Managment tool",
    license="GNU General Public License",
    keywords="shortcuts instances sc",
    url="https://github.com/Apostolos-Delis/shortcut",

    # could also include long_description, download_url, classifiers, etc.
)

if __name__ == "__main__":
    pass
