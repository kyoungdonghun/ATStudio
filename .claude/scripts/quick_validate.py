#!/usr/bin/env python3
"""
Quick Documentation Validation

Lightweight validation for pre-commit hooks.
Runs in <1 second for typical documentation changes.

Usage:
    python3 .claude/scripts/quick_validate.py

Exit codes:
    0 - All validations passed
    1 - Validation failed
"""

import sys
import re
import json
from pathlib import Path
from typing import Dict, List, Tuple


class QuickValidator:
    """Fast documentation validation for pre-commit hooks."""

    def __init__(self, repo_root: Path = None):
        self.repo_root = repo_root or Path(__file__).parent.parent.parent
        self.docs_dir = self.repo_root / "docs"
        self.errors: List[str] = []

    def validate_all(self) -> bool:
        """Run all validations. Returns True if all pass."""
        print("📚 Documentation validation")
        print("━" * 40)

        checks = [
            ("Index counts", self.check_index_counts),
            ("Document paths", self.check_document_paths),
            ("Internal links", self.check_internal_links),
        ]

        for name, check_fn in checks:
            print(f"\n🔍 {name}...", end=" ", flush=True)
            try:
                if check_fn():
                    print("✅")
                else:
                    print("❌")
                    return False
            except Exception as e:
                print(f"❌ Error: {e}")
                self.errors.append(f"{name}: {e}")
                return False

        print("\n" + "━" * 40)
        print("✅ All checks passed\n")
        return True

    def check_index_counts(self) -> bool:
        """Verify docs/index.md counts match actual file counts."""
        # Categories to check (directory -> max_depth)
        categories = {
            'architecture': 1,
            'design': None,  # recursive
            'guides': 1,
            'policies': 1,
            'standards': 1,
            'templates': 1,
            'registry': 1,
            'adr': 1,
            'analysis': 1,
            'eval': 1,
        }

        # Count actual files
        actual_counts = {}
        for category, max_depth in categories.items():
            cat_dir = self.docs_dir / category
            if not cat_dir.exists():
                actual_counts[category] = 0
                continue

            if max_depth == 1:
                # Non-recursive
                files = [f for f in cat_dir.glob("*.md") if f.name != "index.md"]
            else:
                # Recursive
                files = [f for f in cat_dir.rglob("*.md") if f.name != "index.md"]

            actual_counts[category] = len(files)

        # Parse index.md
        index_path = self.docs_dir / "index.md"
        if not index_path.exists():
            self.errors.append("docs/index.md not found")
            return False

        index_content = index_path.read_text()

        # Extract counts from Document Overview table
        # Format: | Category | 9 | [Index](path) | Description |
        index_counts = {}
        in_table = False

        for line in index_content.split('\n'):
            if '| Category |' in line:
                in_table = True
                continue
            if in_table and line.startswith('|'):
                parts = [p.strip() for p in line.split('|')]
                if len(parts) >= 4 and parts[1] and parts[2]:
                    # parts[1] = category name, parts[2] = count
                    category = parts[1].lower()
                    try:
                        count = int(parts[2])
                        index_counts[category] = count
                    except ValueError:
                        continue
            elif in_table and not line.strip().startswith('|'):
                break

        # Compare
        mismatches = []
        for category in categories:
            actual = actual_counts.get(category, 0)
            indexed = index_counts.get(category, -1)

            if actual != indexed:
                mismatches.append(f"{category.capitalize()}: index={indexed}, actual={actual}")

        if mismatches:
            self.errors.append("Index count mismatches:\n  " + "\n  ".join(mismatches))
            print(f"\n❌ {len(mismatches)} mismatch(es):")
            for m in mismatches:
                print(f"  - {m}")
            print("\n💡 Fix: Run '/sync-docs-index --fix'")
            return False

        return True

    def check_document_paths(self) -> bool:
        """Verify all referenced docs/ paths exist."""
        broken_paths = []

        # Check CLAUDE.md
        claude_md = self.repo_root / "CLAUDE.md"
        if claude_md.exists():
            content = claude_md.read_text()
            # Match `docs/...` patterns
            for match in re.finditer(r'`(docs/[^`]+)`', content):
                path_str = match.group(1)
                # Skip wildcard patterns
                if '*' in path_str:
                    continue
                path = self.repo_root / path_str
                if not path.exists():
                    broken_paths.append(f"CLAUDE.md: {path_str}")

        # Check context-injection-rules.json
        rules_json = self.repo_root / ".claude/config/context-injection-rules.json"
        if rules_json.exists():
            content = rules_json.read_text()
            # Match "docs/..." patterns
            for match in re.finditer(r'"(docs/[^"]+)"', content):
                path_str = match.group(1)
                # Skip wildcard patterns
                if '*' in path_str:
                    continue
                # Skip directory patterns (ending with /)
                if path_str.endswith('/'):
                    continue
                # Skip if it doesn't look like a file path (.md, .json, etc.)
                if not any(path_str.endswith(ext) for ext in ['.md', '.json', '.py', '.sh', '.yaml', '.yml']):
                    continue
                path = self.repo_root / path_str
                if not path.exists():
                    broken_paths.append(f"context-injection-rules.json: {path_str}")

        if broken_paths:
            self.errors.append("Broken paths:\n  " + "\n  ".join(broken_paths))
            print(f"\n❌ {len(broken_paths)} broken path(s):")
            for p in broken_paths[:5]:  # Show first 5
                print(f"  - {p}")
            if len(broken_paths) > 5:
                print(f"  ... and {len(broken_paths) - 5} more")
            print("\n💡 Fix: Update paths in CLAUDE.md or context-injection-rules.json")
            return False

        return True

    def check_internal_links(self) -> bool:
        """Verify internal markdown links are valid."""
        broken_links = []

        # Get all markdown files
        md_files = list(self.docs_dir.rglob("*.md"))

        for md_file in md_files:
            content = md_file.read_text()

            # Match markdown links: [text](path)
            for match in re.finditer(r'\[([^\]]+)\]\(([^)]+)\)', content):
                link_text = match.group(1)
                link_path = match.group(2)

                # Skip external links
                if link_path.startswith(('http://', 'https://', '#', 'mailto:')):
                    continue

                # Skip anchor-only links
                if link_path.startswith('#'):
                    continue

                # Skip placeholders/templates
                placeholder_patterns = ['path', 'url', 'link', 'file', 'relative', 'absolute', 'example']
                if any(p in link_path.lower() for p in placeholder_patterns):
                    continue

                # Resolve relative path
                if link_path.startswith('/'):
                    # Absolute from repo root
                    target = self.repo_root / link_path.lstrip('/')
                else:
                    # Relative to current file
                    target = (md_file.parent / link_path).resolve()

                # Check if target exists
                if not target.exists():
                    rel_source = md_file.relative_to(self.repo_root)
                    broken_links.append(f"{rel_source}: [{link_text}]({link_path})")

        if broken_links:
            self.errors.append("Broken links:\n  " + "\n  ".join(broken_links))
            print(f"\n❌ {len(broken_links)} broken link(s):")
            for link in broken_links[:5]:  # Show first 5
                print(f"  - {link}")
            if len(broken_links) > 5:
                print(f"  ... and {len(broken_links) - 5} more")
            print("\n💡 Fix: Run '/validate-docs' for full details")
            return False

        return True


def main():
    """Main entry point."""
    validator = QuickValidator()

    if validator.validate_all():
        sys.exit(0)
    else:
        print("\n❌ Validation failed\n")
        sys.exit(1)


if __name__ == '__main__':
    main()
