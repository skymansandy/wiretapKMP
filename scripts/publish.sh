#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# All publishable modules in dependency order
ALL_MODULES=(
    "wiretap-core"
    "wiretap-ktor"
    "wiretap-ktor-noop"
    "wiretap-okhttp"
    "wiretap-okhttp-noop"
    "wiretap-urlsession"
    "wiretap-urlsession-noop"
)

# Modules that support SPM publishing via KMMBridge
KMMBRIDGE_MODULES=("wiretap-urlsession")

# Colors
BOLD='\033[1m'
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
RESET='\033[0m'

usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Publish Wiretap KMP artifacts to local or remote Maven repositories,
and SPM packages via KMMBridge.

When run without flags, an interactive menu guides you through the options.
Flags skip the corresponding interactive prompts.

OPTIONS:
    --local              Publish to mavenLocal (~/.m2/repository)
    --remote             Publish to remote Maven repository (requires credentials)
    --all                Publish all modules (skip module selection prompt)
    --spm                Run spmDevBuild for URLSession modules (local SPM dev build)
    --module <name>      Publish only a specific module (can be repeated)
    --version <version>  Override the version (default: from gradle.properties)
    --dry-run            Print what would be executed without running
    -h, --help           Show this help message

EXAMPLES:
    # Interactive mode
    $(basename "$0")

    # Non-interactive: all modules to mavenLocal
    $(basename "$0") --local --all

    # Non-interactive: specific modules to remote
    $(basename "$0") --remote --module wiretap-core --module wiretap-ktor

    # Interactive target selection, but publish all
    $(basename "$0") --all

ENVIRONMENT VARIABLES (for --remote):
    ORG_GRADLE_PROJECT_mavenCentralUsername   Sonatype Central Portal token username
    ORG_GRADLE_PROJECT_mavenCentralPassword   Sonatype Central Portal token password
    ORG_GRADLE_PROJECT_signingInMemoryKey     ASCII-armored GPG private key
    ORG_GRADLE_PROJECT_signingInMemoryKeyPassword  GPG key passphrase
EOF
    exit 0
}

# Defaults
TARGET=""
TARGET_FROM_FLAG=false
SPM=false
DRY_RUN=false
VERSION_OVERRIDE=""
PUBLISH_ALL=false
SELECTED_MODULES=()

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --local)    TARGET="local";  TARGET_FROM_FLAG=true; shift ;;
        --remote)   TARGET="remote"; TARGET_FROM_FLAG=true; shift ;;
        --all)      PUBLISH_ALL=true;          shift ;;
        --spm)      SPM=true;                  shift ;;
        --module)   SELECTED_MODULES+=("$2");  shift 2 ;;
        --version)  VERSION_OVERRIDE="$2";     shift 2 ;;
        --dry-run)  DRY_RUN=true;              shift ;;
        -h|--help)  usage ;;
        *)
            echo -e "${RED}Error: Unknown option '$1'${RESET}"
            echo ""
            usage
            ;;
    esac
done

# ── Interactive prompts (only for values not already set via flags) ───────────

prompt_target() {
    if [[ -n "$TARGET" ]]; then
        return
    fi
    echo ""
    echo -e "${BOLD}Where do you want to publish?${RESET}"
    echo ""
    echo "  1) Local   (~/.m2/repository)"
    echo "  2) Remote  (Maven repository)"
    echo ""
    while true; do
        read -r -p "Select [1/2]: " choice
        case "$choice" in
            1) TARGET="local";  break ;;
            2) TARGET="remote"; break ;;
            *) echo -e "${YELLOW}Please enter 1 or 2.${RESET}" ;;
        esac
    done
}

prompt_modules() {
    # Skip if --all or --module was provided
    if [[ "$PUBLISH_ALL" == true ]] || [[ ${#SELECTED_MODULES[@]} -gt 0 ]]; then
        return
    fi
    # Non-interactive mode: default to all
    if [[ "$TARGET_FROM_FLAG" == true ]]; then
        PUBLISH_ALL=true
        return
    fi
    echo ""
    echo -e "${BOLD}Which modules do you want to publish?${RESET}"
    echo ""
    echo "  0) All modules"
    for i in "${!ALL_MODULES[@]}"; do
        echo "  $((i + 1))) ${ALL_MODULES[$i]}"
    done
    echo ""
    echo -e "  ${CYAN}Enter numbers separated by spaces (e.g. \"1 3 4\"), or 0 for all:${RESET}"
    while true; do
        read -r -p "> " input
        if [[ -z "$input" ]]; then
            echo -e "${YELLOW}Please make a selection.${RESET}"
            continue
        fi

        valid=true
        for num in $input; do
            if ! [[ "$num" =~ ^[0-9]+$ ]] || [[ "$num" -gt ${#ALL_MODULES[@]} ]]; then
                echo -e "${YELLOW}Invalid selection: $num${RESET}"
                valid=false
                break
            fi
        done

        if [[ "$valid" == true ]]; then
            for num in $input; do
                if [[ "$num" -eq 0 ]]; then
                    PUBLISH_ALL=true
                    SELECTED_MODULES=()
                    break
                fi
                SELECTED_MODULES+=("${ALL_MODULES[$((num - 1))]}")
            done
            break
        fi
    done
}

prompt_spm() {
    # Skip if --spm was already set, or running non-interactively
    if [[ "$SPM" == true ]] || [[ "$TARGET_FROM_FLAG" == true ]]; then
        return
    fi

    local has_urlsession=false
    for module in "${KMMBRIDGE_MODULES[@]}"; do
        if should_publish "$module"; then
            has_urlsession=true
            break
        fi
    done

    if [[ "$has_urlsession" == false ]]; then
        return
    fi

    echo ""
    echo -e "${BOLD}Also run SPM dev build (spmDevBuild) for URLSession modules?${RESET}"
    while true; do
        read -r -p "[y/N]: " yn
        case "$yn" in
            [Yy]*) SPM=true;  break ;;
            [Nn]*|"") SPM=false; break ;;
            *) echo -e "${YELLOW}Please enter y or n.${RESET}" ;;
        esac
    done
}

prompt_version() {
    if [[ -n "$VERSION_OVERRIDE" ]] || [[ "$TARGET_FROM_FLAG" == true ]]; then
        return
    fi

    local default_version
    default_version=$(grep 'wiretap.version=' "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)

    echo ""
    echo -e "${BOLD}Version to publish:${RESET} ${CYAN}${default_version}${RESET}"
    read -r -p "Press Enter to keep, or type a new version: " input
    if [[ -n "$input" ]]; then
        VERSION_OVERRIDE="$input"
    fi
}

# ── Helpers ──────────────────────────────────────────────────────────────────

should_publish() {
    local module="$1"
    if [[ "$PUBLISH_ALL" == true ]] || [[ ${#SELECTED_MODULES[@]} -eq 0 ]]; then
        return 0
    fi
    for selected in "${SELECTED_MODULES[@]}"; do
        if [[ "$selected" == "$module" ]]; then
            return 0
        fi
    done
    return 1
}

is_kmmbridge_module() {
    local module="$1"
    for km in "${KMMBRIDGE_MODULES[@]}"; do
        if [[ "$km" == "$module" ]]; then
            return 0
        fi
    done
    return 1
}

run_cmd() {
    if [[ "$DRY_RUN" == true ]]; then
        echo -e "  ${YELLOW}[dry-run]${RESET} $*"
    else
        echo -e "  ${CYAN}>>>${RESET} $*"
        "$@"
    fi
}

# ── Run interactive prompts ──────────────────────────────────────────────────

prompt_target
prompt_modules
prompt_spm
prompt_version

# ── Build configuration ─────────────────────────────────────────────────────

VERSION_FLAG=""
if [[ -n "$VERSION_OVERRIDE" ]]; then
    VERSION_FLAG="-Pwiretap.version=$VERSION_OVERRIDE"
fi

if [[ "$TARGET" == "local" ]]; then
    MAVEN_TASK="publishToMavenLocal"
else
    MAVEN_TASK="publishAndReleaseToMavenCentral"
fi

cd "$PROJECT_ROOT"

# ── Summary ──────────────────────────────────────────────────────────────────

echo ""
echo -e "${BOLD}==========================================${RESET}"
echo -e "${BOLD} Wiretap KMP Publisher${RESET}"
echo -e "${BOLD}==========================================${RESET}"
echo -e "  Target:  ${GREEN}${TARGET}${RESET}"
if [[ -n "$VERSION_OVERRIDE" ]]; then
    echo -e "  Version: ${GREEN}${VERSION_OVERRIDE}${RESET}"
else
    echo -e "  Version: ${GREEN}$(grep 'wiretap.version=' "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)${RESET}"
fi
if [[ "$SPM" == true ]]; then
    echo -e "  SPM:     ${GREEN}yes${RESET}"
fi
echo -e "  Modules:"
for module in "${ALL_MODULES[@]}"; do
    if should_publish "$module"; then
        echo -e "    - ${module}"
    fi
done
if [[ "$DRY_RUN" == true ]]; then
    echo -e "  Mode:    ${YELLOW}dry-run${RESET}"
fi
echo -e "${BOLD}==========================================${RESET}"
echo ""

# ── Publish ──────────────────────────────────────────────────────────────────

for module in "${ALL_MODULES[@]}"; do
    if ! should_publish "$module"; then
        continue
    fi

    echo -e "${BOLD}--- ${module} ---${RESET}"
    run_cmd ./gradlew ":${module}:${MAVEN_TASK}" $VERSION_FLAG

    if [[ "$SPM" == true ]] && is_kmmbridge_module "$module"; then
        run_cmd ./gradlew ":${module}:spmDevBuild" $VERSION_FLAG
    fi

    echo ""
done

echo -e "${GREEN}${BOLD}Done!${RESET}"
