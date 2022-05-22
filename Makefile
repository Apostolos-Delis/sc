.PHONY: build
build: clean
	@python3 setup.py sdist bdist_wheel

.PHONY: deploy
deploy: build
	@twine upload dist/*

.PHONY: clean
clean:
	@rm -rf build dist
