#!/bin/bash
#
# Pre-commit hook for documentation integrity
# Runs when docs/ files are changed
#

set -e

# Check if docs/ directory has changes
if ! git diff --cached --name-only | grep -q '^docs/'; then
  exit 0
fi

echo ""
echo "📚 Documentation changes detected"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check 1: Index count verification
echo ""
echo "🔍 [1/3] Checking docs index counts..."
if ! /sync-docs-index --check 2>&1 | grep -q "All counts match"; then
  echo ""
  echo "❌ Index count mismatch detected"
  echo "💡 Fix: Run '/sync-docs-index --fix' to update counts"
  echo ""
  exit 1
fi
echo "✅ Index counts match"

# Check 2: Document path validation
echo ""
echo "🔍 [2/3] Validating document paths..."
if ! bash .claude/scripts/check_doc_paths.sh; then
  echo ""
  echo "❌ Broken document paths found"
  echo "💡 Fix: Update paths in CLAUDE.md or context-injection-rules.json"
  echo ""
  exit 1
fi
echo "✅ All document paths valid"

# Check 3: Link validation
echo ""
echo "🔍 [3/3] Validating documentation links..."
if ! /validate-docs 2>&1 | grep -q "validation passed"; then
  echo ""
  echo "❌ Broken links found"
  echo "💡 Fix: Run '/validate-docs' for details"
  echo ""
  exit 1
fi
echo "✅ All links valid"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ All documentation checks passed"
echo ""

exit 0
