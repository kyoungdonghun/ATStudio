---
version: 1.0
last_updated: 2026-01-29
project: system
owner: RE
category: template
status: stable
dependencies:
  - path: ../guides/runbook-and-postmortem.md
    reason: Post-analysis guide reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# Postmortem (RCA) Template

> Purpose: Turn incidents into **recurrence prevention assets**.
> Principle: Fact-based, chronological, actions limited to 1-3 items.

## 1) Summary

- **Incident ID**: INC-YYYYMMDD-###
- **Occurrence time/End time**:
- **Severity (Criticality)**: HIGH | MEDIUM
- **One-line summary**:

## 2) Impact

- **Impact scope**:
- **User impact**:
- **Data/security impact**: (if any)

## 3) Timeline

- T-0:
- T+…:

## 4) Root Cause Analysis

- **Direct cause**:
- **Root cause**:
- **Detection failure/delay cause**: (if any)

## 5) What worked / What didn't work

- **Worked**:
- **Didn't work**:

## 6) Recurrence Prevention Actions (1-3 items)

Write each action as "who/what/by when".

- [ ] **Action 1**: Owner= , Due= , WI=
- [ ] **Action 2**: Owner= , Due= , WI=
- [ ] **Action 3**: Owner= , Due= , WI=

## 7) Traceability Links

- **Related WI**:
- **Related ADR**:
- **Related Assets**:
- **Golden Set update**: (added/enhanced cases)
