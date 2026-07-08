.PHONY: build test test-unit dev clean help

# Robust path resolution: works from any directory
DOCKER_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
REPO_ROOT  := $(realpath $(DOCKER_DIR)/..)
COMPOSE    := docker compose -f $(DOCKER_DIR)/docker-compose.yml

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Build Docker image
	$(COMPOSE) build

test: ## Run all tests (unit + GUI, ~90s)
	$(COMPOSE) run --rm test

test-unit: ## Run non-GUI tests only (fast, stable)
	$(COMPOSE) run --rm test ./gradlew --no-daemon clean \
		:StandardModules:test :Filters:test :Renderers:test \
		:PreferencesPlugin:test :PrimitiveProviders:test \
		--continue

dev: ## Interactive shell in Docker
	$(COMPOSE) run --rm dev

assemble: ## Build distribution (no tests)
	$(COMPOSE) run --rm build

clean: ## Clean build artifacts inside Docker
	$(COMPOSE) run --rm test ./gradlew --no-daemon clean
