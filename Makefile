SHELL := /bin/bash

OUT_DIR := out
SRC_FILES := $(shell find src -name "*.java")
MAIN_CLASS := src.main.Game

.PHONY: build run clean

build:
	@echo "Compiling Java files..."
	@mkdir -p "$(OUT_DIR)"
	@javac -d "$(OUT_DIR)" $(SRC_FILES)

run: build
	@echo "Running Cyber Defence Game..."
	@java -cp "$(OUT_DIR)" $(MAIN_CLASS)

clean:
	@echo "Cleaning build artifacts..."
	@rm -rf "$(OUT_DIR)"
