SHELL := bash

APP_NAME := CyberDefenceGame
APP_VERSION ?= 1.0.0
APP_VENDOR ?= taaliis4
MAIN_CLASS := src.main.Game

OUT_DIR := out
CLASSES_DIR := $(OUT_DIR)/classes
PACKAGE_INPUT_DIR := $(OUT_DIR)/package-input
DIST_DIR := dist
PACKAGE_DIR := $(DIST_DIR)/packages
JAR_NAME := $(APP_NAME).jar
JAR_PATH := $(DIST_DIR)/$(JAR_NAME)

SRC_FILES := $(shell find src -name "*.java" | sort)

JAVA_BIN_DIR := $(if $(JAVA_HOME),$(JAVA_HOME)/bin,)

JAVAC ?= $(if $(wildcard $(JAVA_BIN_DIR)/javac),$(JAVA_BIN_DIR)/javac,$(shell command -v javac 2>/dev/null))
JAVA ?= $(if $(wildcard $(JAVA_BIN_DIR)/java),$(JAVA_BIN_DIR)/java,$(shell command -v java 2>/dev/null))
JAR ?= $(if $(wildcard $(JAVA_BIN_DIR)/jar),$(JAVA_BIN_DIR)/jar,$(shell command -v jar 2>/dev/null))
JPACKAGE ?= $(if $(wildcard $(JAVA_BIN_DIR)/jpackage),$(JAVA_BIN_DIR)/jpackage,$(shell command -v jpackage 2>/dev/null))

ifeq ($(OS),Windows_NT)
HOST_OS := Windows
else
HOST_OS := $(shell uname -s)
endif

LINUX_PACKAGE_TYPE :=
ifneq ($(shell command -v dpkg-deb 2>/dev/null),)
LINUX_PACKAGE_TYPE := deb
else ifneq ($(shell command -v rpmbuild 2>/dev/null),)
LINUX_PACKAGE_TYPE := rpm
endif

ifeq ($(HOST_OS),Darwin)
DEFAULT_PACKAGE_TYPE := dmg
else ifeq ($(HOST_OS),Windows)
DEFAULT_PACKAGE_TYPE := exe
else ifeq ($(HOST_OS),Linux)
DEFAULT_PACKAGE_TYPE := $(if $(LINUX_PACKAGE_TYPE),$(LINUX_PACKAGE_TYPE),app-image)
else
DEFAULT_PACKAGE_TYPE := app-image
endif

.PHONY: build compile jar app-image native-installer package package-only build-mac build-windows build-linux run clean help

build: jar app-image native-installer
	@echo "Build complete. Artifacts are in $(DIST_DIR)/"

compile:
	@if [ -z "$(JAVAC)" ]; then echo "javac not found in PATH."; exit 1; fi
	@echo "Compiling Java files..."
	@rm -rf "$(CLASSES_DIR)"
	@mkdir -p "$(CLASSES_DIR)"
	@$(JAVAC) -d "$(CLASSES_DIR)" $(SRC_FILES)

jar: compile
	@if [ -z "$(JAR)" ]; then echo "jar not found in PATH."; exit 1; fi
	@echo "Creating runnable JAR..."
	@mkdir -p "$(DIST_DIR)" "$(PACKAGE_INPUT_DIR)"
	@$(JAR) --create --file "$(JAR_PATH)" --main-class "$(MAIN_CLASS)" -C "$(CLASSES_DIR)" .
	@rm -rf "$(PACKAGE_INPUT_DIR)"
	@mkdir -p "$(PACKAGE_INPUT_DIR)"
	@cp "$(JAR_PATH)" "$(PACKAGE_INPUT_DIR)/$(JAR_NAME)"

app-image: jar
	@if [ -z "$(JPACKAGE)" ]; then echo "jpackage not found in PATH."; exit 1; fi
	@echo "Creating app image..."
	@mkdir -p "$(PACKAGE_DIR)"
	@rm -rf "$(PACKAGE_DIR)/$(APP_NAME)" "$(PACKAGE_DIR)/$(APP_NAME).app"
	@$(JPACKAGE) \
		--type app-image \
		--name "$(APP_NAME)" \
		--app-version "$(APP_VERSION)" \
		--vendor "$(APP_VENDOR)" \
		--input "$(PACKAGE_INPUT_DIR)" \
		--main-jar "$(JAR_NAME)" \
		--main-class "$(MAIN_CLASS)" \
		--dest "$(PACKAGE_DIR)"

native-installer:
	@if [ "$(DEFAULT_PACKAGE_TYPE)" = "app-image" ]; then \
		echo "No native installer type detected for $(HOST_OS). App image only."; \
	else \
		$(MAKE) package-only PACKAGE_TYPE="$(DEFAULT_PACKAGE_TYPE)"; \
	fi

package: jar package-only

package-only:
	@if [ -z "$(JPACKAGE)" ]; then echo "jpackage not found in PATH."; exit 1; fi
	@if [ -z "$(PACKAGE_TYPE)" ]; then echo "Set PACKAGE_TYPE=<app-image|dmg|pkg|exe|msi|deb|rpm>."; exit 1; fi
	@echo "Packaging $(PACKAGE_TYPE)..."
	@mkdir -p "$(PACKAGE_DIR)"
	@rm -rf "$(PACKAGE_DIR)/$(APP_NAME)" "$(PACKAGE_DIR)/$(APP_NAME).app"
	@rm -f "$(PACKAGE_DIR)/$(APP_NAME)"*.dmg "$(PACKAGE_DIR)/$(APP_NAME)"*.pkg "$(PACKAGE_DIR)/$(APP_NAME)"*.exe "$(PACKAGE_DIR)/$(APP_NAME)"*.msi "$(PACKAGE_DIR)/$(APP_NAME)"*.deb "$(PACKAGE_DIR)/$(APP_NAME)"*.rpm
	@$(JPACKAGE) \
		--type "$(PACKAGE_TYPE)" \
		--name "$(APP_NAME)" \
		--app-version "$(APP_VERSION)" \
		--vendor "$(APP_VENDOR)" \
		--input "$(PACKAGE_INPUT_DIR)" \
		--main-jar "$(JAR_NAME)" \
		--main-class "$(MAIN_CLASS)" \
		--dest "$(PACKAGE_DIR)"

build-mac:
	@if [ "$(HOST_OS)" != "Darwin" ]; then echo "macOS packages must be built on macOS."; exit 1; fi
	@$(MAKE) app-image
	@$(MAKE) package-only PACKAGE_TYPE=dmg

build-windows:
	@if [ "$(HOST_OS)" != "Windows" ]; then echo "Windows packages must be built on Windows."; exit 1; fi
	@$(MAKE) app-image
	@$(MAKE) package-only PACKAGE_TYPE=exe

build-linux:
	@if [ "$(HOST_OS)" != "Linux" ]; then echo "Linux packages must be built on Linux."; exit 1; fi
	@$(MAKE) app-image
	@if [ -n "$(LINUX_PACKAGE_TYPE)" ]; then \
		$(MAKE) package-only PACKAGE_TYPE="$(LINUX_PACKAGE_TYPE)"; \
	else \
		echo "No Linux native package tool detected. App image created instead."; \
	fi

run: compile
	@if [ -z "$(JAVA)" ]; then echo "java not found in PATH."; exit 1; fi
	@echo "Running Cyber Defence Game..."
	@$(JAVA) -cp "$(CLASSES_DIR)" $(MAIN_CLASS)

clean:
	@echo "Cleaning build artifacts..."
	@rm -rf "$(OUT_DIR)" "$(DIST_DIR)"

help:
	@echo "Targets:"
	@echo "  make build         Compile, jar, app-image, and create the host-native installer"
	@echo "  make run           Compile and launch the game"
	@echo "  make jar           Build a runnable JAR at $(JAR_PATH)"
	@echo "  make app-image     Build a bundled app image in $(PACKAGE_DIR)"
	@echo "  make package PACKAGE_TYPE=<type>   Build a specific jpackage artifact"
	@echo "  make build-mac     Build a .dmg on macOS"
	@echo "  make build-windows Build an .exe on Windows"
	@echo "  make build-linux   Build a .deb/.rpm on Linux when supported"
	@echo "  make clean         Remove out/ and dist/"
