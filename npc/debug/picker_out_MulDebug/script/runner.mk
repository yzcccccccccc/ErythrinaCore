WORK_DIR = $(shell pwd)/src
PYTHON 	= python3

run:
	@mkdir report -p
	PYTHONPATH=. $(PYTHON) $(WORK_DIR)/__init__.py

wave:
	gtkwave report/mul.fst

clean:
	-@rm -rf report