#!/bin/bash

# Configuration
OUT_DIR="out"
LIB_DIR="lib"
JUNIT_VERSION="1.10.0"
JUNIT_JAR_NAME="junit-platform-console-standalone-${JUNIT_VERSION}.jar"
JUNIT_JAR_PATH="${LIB_DIR}/${JUNIT_JAR_NAME}"
JUNIT_DOWNLOAD_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/${JUNIT_JAR_NAME}"

# Ensure the script exits on any error
set -e

# --- Helper Functions ---

# Function to print info messages
info() {
    echo "[INFO] $1"
}

# Function to print error messages
error() {
    echo "[ERROR] $1" >&2
    exit 1
}

# --- Core Functions ---

# Function to clean up build artifacts
clean() {
    info "Cleaning up build artifacts and downloaded libraries..."
    rm -rf "${OUT_DIR}"
    rm -rf "${LIB_DIR}"
    info "Cleanup complete."
}

# Function to download dependencies (JUnit)
download_deps() {
    if [ -f "${JUNIT_JAR_PATH}" ]; then
        info "JUnit already downloaded."
        return
    fi
    info "Downloading JUnit Platform Console Standalone..."
    mkdir -p "${LIB_DIR}"
    curl -L -s -o "${JUNIT_JAR_PATH}" "${JUNIT_DOWNLOAD_URL}" || error "Failed to download JUnit."
    info "JUnit downloaded successfully."
}

# Function to compile source and test files
compile() {
    info "Compiling Java files..."
    mkdir -p "${OUT_DIR}"
    
    # Find all java files in src and test directories
    SRC_FILES=$(find src -name "*.java")
    TEST_FILES=$(find test -name "*.java")

    if [ -z "$SRC_FILES" ] && [ -z "$TEST_FILES" ]; then
        error "No Java files found to compile."
    fi

    javac -d "${OUT_DIR}" -cp ".:${JUNIT_JAR_PATH}" ${SRC_FILES} ${TEST_FILES} || error "Compilation failed."
    info "Compilation successful."
}

# Function to run tests
run_tests() {
    info "Running tests..."
    if [ ! -d "${OUT_DIR}" ]; then
        error "Output directory '${OUT_DIR}' not found. Please compile first."
    fi
    java -jar "${JUNIT_JAR_PATH}" --class-path "${OUT_DIR}" --scan-class-path || error "Test run failed."
    info "All tests passed."
}

# --- Main script logic ---

# Main function to run all steps
test_all() {
    download_deps
    compile
    run_tests
    clean
}

# Modular command handling
if [ -z "$1" ]; then
    test_all
    exit 0
fi

case "$1" in
    clean)
        clean
        ;;
    compile)
        download_deps
        compile
        ;;
    test)
        download_deps
        if [ ! -d "${OUT_DIR}" ]; then
            compile
        fi
        run_tests
        ;;
    *)
        echo "Usage: $0 {compile|test|clean}"
        exit 1
        ;;
esac
