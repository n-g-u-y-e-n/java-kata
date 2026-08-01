gradle := if os() == "windows" { "gradlew.bat" } else { "./gradlew" }

# List available recipes
default:
  @just --list

# Show all Gradle tasks
tasks:
  {{gradle}} tasks --all

# Clean build outputs
clean:
  {{gradle}} clean

# Apply Spotless formatting
format:
  {{gradle}} spotlessApply

# Check Spotless formatting
format-check:
  {{gradle}} spotlessCheck

# Run tests for all modules
test:
  {{gradle}} test --no-configuration-cache --rerun-tasks

# Run tests for one module
test-module module="01-functional-shift:01-declarative-aggregator":
  {{gradle}} :{{module}}:test

# Run all verification checks
check:
  {{gradle}} check --no-configuration-cache --rerun-tasks

# Build all modules
build:
  {{gradle}} build

# Run the CI-equivalent pipeline
ci: clean check
