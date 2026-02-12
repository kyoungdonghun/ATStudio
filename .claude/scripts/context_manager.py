import json
import os
import glob
import time
import re
import argparse
from typing import Dict, Any, List, Iterable, Tuple

# Configuration
MEMORY_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "memory")
DOCS_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "docs")
AGENT_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)))
CONTEXT_TRIGGERS_PATH = os.path.join(AGENT_DIR, "config", "context-triggers.json")
WORKSPACE_JSON_PATH = os.path.join(AGENT_DIR, "config", "workspace.json")
INJECTION_RULES_PATH = os.path.join(AGENT_DIR, "config", "context-injection-rules.json")
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))

# Tier 0 Constants (Absolute Paths will be resolved dynamically)
TIER_0_DOCS = [
    "standards/core-principles.md",
    "standards/documentation-standards.md", # Explicitly High Priority
    "standards/development-standards.md",
    "standards/glossary.md"
]

class ContextManager:
    def __init__(self):
        os.makedirs(MEMORY_DIR, exist_ok=True)

    def load_tier0_context(self) -> str:
        """
        Loads Mandatory Tier 0 Documents.
        Strict adherence to Documentation Standards is enforced here.
        """
        context_buffer = []
        context_buffer.append("=== TIER 0: MANDATORY CONTEXT (DO NOT IGNORE) ===\n")
        
        for relative_path in TIER_0_DOCS:
            full_path = os.path.join(DOCS_DIR, relative_path)
            try:
                with open(full_path, "r", encoding="utf-8") as f:
                    content = f.read()
                    context_buffer.append(f"--- START: {relative_path} ---")
                    context_buffer.append(content)
                    context_buffer.append(f"--- END: {relative_path} ---\n")
            except FileNotFoundError:
                print(f"[WARN] Tier 0 Document not found: {full_path}")
                # Critical mechanism: If Tier 0 is missing, we might want to halt or warn loudly
                context_buffer.append(f"[CRITICAL WARNING] MISSING TIER 0 DOC: {relative_path}")

        return "\n".join(context_buffer)

    def _load_trigger_config(self) -> Dict[str, Any]:
        """
        Loads context triggers config for on-demand document injection.
        """
        try:
            with open(CONTEXT_TRIGGERS_PATH, "r", encoding="utf-8") as f:
                return json.load(f)
        except FileNotFoundError:
            print(f"[WARN] context-triggers.json not found: {CONTEXT_TRIGGERS_PATH}")
            return {"triggers": []}
        except Exception as e:
            print(f"[ERROR] Failed to load context triggers: {e}")
            return {"triggers": []}

    @staticmethod
    def _normalize_trigger_doc_path(path: str) -> str:
        """
        Trigger config stores paths like 'docs/...'. Internally we load from DOCS_DIR.
        """
        if path.startswith("docs/"):
            return path[len("docs/") :]
        return path

    def select_docs_by_triggers(self, text: str) -> Tuple[List[str], List[str]]:
        """
        Returns (doc_paths, matched_categories) based on trigger regex matches.

        doc_paths are relative to DOCS_DIR (e.g. 'guides/traceability.md').
        """
        cfg = self._load_trigger_config()
        triggers = cfg.get("triggers", []) or []

        selected: List[str] = []
        matched_categories: List[str] = []
        for t in triggers:
            try:
                category = str(t.get("category", ""))
                regex = str(t.get("regex", ""))
                load = t.get("load", []) or []
                if not regex or not load:
                    continue
                if re.search(regex, text or "", flags=re.IGNORECASE):
                    matched_categories.append(category)
                    for p in load:
                        selected.append(self._normalize_trigger_doc_path(str(p)))
            except re.error as e:
                print(f"[WARN] Invalid trigger regex for category='{t.get('category')}': {e}")

        # preserve order + de-dupe
        deduped: List[str] = []
        seen = set()
        for p in selected:
            if p in seen:
                continue
            seen.add(p)
            deduped.append(p)
        return deduped, matched_categories

    def load_docs_context(self, relative_paths: Iterable[str], title: str) -> str:
        """
        Loads arbitrary docs under DOCS_DIR by relative paths.
        """
        context_buffer: List[str] = []
        context_buffer.append(f"=== {title} ===\n")

        for relative_path in relative_paths:
            full_path = os.path.join(DOCS_DIR, relative_path)
            try:
                with open(full_path, "r", encoding="utf-8") as f:
                    content = f.read()
                context_buffer.append(f"--- START: {relative_path} ---")
                context_buffer.append(content)
                context_buffer.append(f"--- END: {relative_path} ---\n")
            except FileNotFoundError:
                context_buffer.append(f"[WARN] Missing doc: {relative_path}")
            except Exception as e:
                context_buffer.append(f"[WARN] Failed to load doc '{relative_path}': {e}")

        return "\n".join(context_buffer)

    def _load_json_config(self, path: str) -> Dict[str, Any]:
        """Loads a JSON config file."""
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except FileNotFoundError:
            print(f"[WARN] Config not found: {path}")
            return {}
        except Exception as e:
            print(f"[ERROR] Failed to load config {path}: {e}")
            return {}

    def _find_project_by_tag(self, workspace: Dict[str, Any], tag: str) -> Dict[str, Any]:
        """Finds a domain project by its tag in workspace.json."""
        if tag == workspace.get("meta_framework", {}).get("tag"):
            return workspace.get("meta_framework", {})
        for project in workspace.get("domain_projects", []):
            if project.get("tag") == tag:
                return project
        return {}

    def load_tech_stack_context(self, project_tag: str, assignee: str) -> str:
        """
        Loads tech-stack-specific documents based on workspace.json project tech_stack
        and context-injection-rules.json tech_stack_documents mapping.

        Returns context string with matched documents, or empty string if no match.
        """
        workspace = self._load_json_config(WORKSPACE_JSON_PATH)
        rules = self._load_json_config(INJECTION_RULES_PATH)

        project = self._find_project_by_tag(workspace, project_tag)
        if not project:
            print(f"[WARN] Project not found for tag: {project_tag}")
            return ""

        tech_stack = project.get("tech_stack", [])
        tech_stack_rules = rules.get("tech_stack_documents", {})
        matched_docs: List[str] = []
        matched_techs: List[str] = []

        for tech in tech_stack:
            config = tech_stack_rules.get(tech)
            if not config:
                continue
            if assignee not in config.get("applicable_agents", []):
                continue
            matched_techs.append(tech)
            for doc_path in config.get("documents", []):
                if doc_path not in matched_docs:
                    matched_docs.append(doc_path)

        if not matched_docs:
            return ""

        context_buffer: List[str] = []
        context_buffer.append(f"=== TECH STACK CONTEXT (matched: {', '.join(matched_techs)}) ===\n")

        for doc_path in matched_docs:
            full_path = os.path.join(PROJECT_ROOT, doc_path)
            try:
                with open(full_path, "r", encoding="utf-8") as f:
                    content = f.read()
                context_buffer.append(f"--- START: {doc_path} ---")
                context_buffer.append(content)
                context_buffer.append(f"--- END: {doc_path} ---\n")
            except FileNotFoundError:
                context_buffer.append(f"[WARN] Missing tech stack doc: {doc_path}")
            except Exception as e:
                context_buffer.append(f"[WARN] Failed to load '{doc_path}': {e}")

        return "\n".join(context_buffer)

    def load_context_for_text(
        self,
        text: str,
        include_tier0: bool = True,
        project_tag: str = "",
        assignee: str = "",
    ) -> str:
        """
        On-demand injection: Tier 0 + trigger-matched docs + tech-stack docs for the given text.
        """
        parts: List[str] = []
        if include_tier0:
            parts.append(self.load_tier0_context())

        doc_paths, matched_categories = self.select_docs_by_triggers(text)
        if matched_categories:
            parts.append(
                "=== CONTEXT TRIGGERS (MATCHED CATEGORIES) ===\n"
                + "\n".join(f"- {c}" for c in matched_categories)
                + "\n"
            )
        if doc_paths:
            parts.append(self.load_docs_context(doc_paths, "TRIGGERED DOCS (ON-DEMAND INJECTION)"))
        else:
            parts.append("=== TRIGGERED DOCS (ON-DEMAND INJECTION) ===\n(none)\n")

        # Tech-stack-based injection
        if project_tag and assignee:
            tech_ctx = self.load_tech_stack_context(project_tag, assignee)
            if tech_ctx:
                parts.append(tech_ctx)

        return "\n".join(parts)

    def save_snapshot(self, role: str, memory_data: Dict[str, Any]) -> str:
        """
        Saves the current agent state/memory to a JSON snapshot.
        """
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        filename = f"snap_{role}_{timestamp}.json"
        filepath = os.path.join(MEMORY_DIR, filename)

        snapshot = {
            "timestamp": timestamp,
            "role": role,
            "memory": memory_data,
            "meta": {
                "version": "1.0",
                "type": "handoff_snapshot"
            }
        }

        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(snapshot, f, indent=2, ensure_ascii=False)
        
        print(f"[ContextManager] Snapshot saved: {filepath}")
        return filepath

    def restore_latest_snapshot(self, role: str) -> Dict[str, Any]:
        """
        Finds and loads the latest snapshot for the given role.
        """
        pattern = os.path.join(MEMORY_DIR, f"snap_{role}_*.json")
        files = glob.glob(pattern)
        
        if not files:
            print(f"[ContextManager] No snapshot found for role: {role}")
            return {}

        latest_file = max(files, key=os.path.getctime)
        try:
            with open(latest_file, "r", encoding="utf-8") as f:
                data = json.load(f)
            print(f"[ContextManager] Restored snapshot: {latest_file}")
            return data.get("memory", {})
        except Exception as e:
            print(f"[ERROR] Failed to load snapshot {latest_file}: {e}")
            return {}

# Example Usage for Testing
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="ContextManager utilities (Tier0 + on-demand docs injection)")
    parser.add_argument("--role", default="PM", help="snapshot role name")
    parser.add_argument("--save-snapshot", action="store_true", help="save a snapshot to .claude/memory/")
    parser.add_argument("--restore-snapshot", action="store_true", help="restore latest snapshot for role")
    parser.add_argument("--text", default="", help="free text to trigger on-demand docs injection")
    parser.add_argument("--include-tier0", action="store_true", help="include Tier 0 docs in output")
    parser.add_argument("--project-tag", default="", help="project tag for tech-stack-based injection (e.g. PMV2, DDS)")
    parser.add_argument("--assignee", default="", help="agent name for tech-stack-based injection (e.g. se, qa, cr)")
    args = parser.parse_args()

    cm = ContextManager()

    if args.save_snapshot:
        cm.save_snapshot(args.role, {"note": "manual snapshot", "text": args.text})

    if args.restore_snapshot:
        mem = cm.restore_latest_snapshot(args.role)
        print(json.dumps(mem, ensure_ascii=False, indent=2))

    if args.text:
        ctx = cm.load_context_for_text(
            args.text,
            include_tier0=args.include_tier0,
            project_tag=args.project_tag,
            assignee=args.assignee,
        )
        print(ctx)
