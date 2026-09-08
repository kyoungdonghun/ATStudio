---
version: 1.2
last_updated: 2026-09-09
project: ATS
owner: se
category: reference
status: active
---

# WI-20260909-ATS-005: Dependency And Multipart Corrections

> Implementation is ready for MA's serialized verification. This report does not claim test/build completion or production readiness.

## Delivered

| Finding | Bounded change |
|---|---|
| DEP-01 | Boot 4.0.2 -> 4.0.8 in build.gradle; no transitive overrides or major migration. Official BOM manages Tomcat 11.0.24, Framework 7.0.9, Security 7.0.7. |
| DEP-02 | Compatible lock-only npm corrections for six development advisory groups and their necessary Browserslist dependencies. Direct package ranges unchanged. |
| OPS-01 | AppConfig sets 128 total multipart parts. Track edits use 8 fixed parts plus separately encoded tagIds, allowing 120 tags at this transport boundary. Notice replacement's 13 parts and certification's 10 parts fit. |
| Regression | AppConfigMultipartBoundaryTest has ten synthetic cases: configuration, upload/edit forms, notice replacement, certification, question attachments, exact 128 parts, 129 rejection, real Spring resolver/production 413 contract, and original unlimited control. |

Production-owned paths changed: `build.gradle`, `frontend/package-lock.json`, `src/main/java/com/atstudio/atstudio/config/AppConfig.java`. New focused test: `src/test/java/com/atstudio/atstudio/config/AppConfigMultipartBoundaryTest.java`.

No handler change was needed: Framework 7.0.9 turns count overflow into `MaxUploadSizeExceededException`; the existing handler returns HTTP 413 with `errorCode=IO_LARGE` and its existing message. The composed test uses that real path, not a test-only error response. HTTP/proxy behavior is not claimed as executed evidence.

## Verification Boundary

- MA's first patched build compiled product sources and produced bootJar, then failed compileTestJava because the synthetic InputBuffer lambda omitted its second abstract method. The explicit doRead/available correction compiled in MA's second run.
- MA's completed second run: 1,825 total, 1,797 passed, nine failed, 19 skipped. All nine failures were this test's Content-Type fixture setup, before multipart parsing; its configuration case passed. XML and actual JAR bytecode identified the uninitialized contentTypeMB cache. The fixture now supplies the real Content-Type MIME header instead of calling the deprecated String setter. Focused and full retries remain pending; full metrics have not passed.
- Executed: authorized `npm audit fix --package-lock-only --ignore-scripts`, exit 0, audit result 0 vulnerabilities against the updated lockfile.
- Executed: scoped diff review, JSON package-boundary checks, 16 parent semver checks, engine compatibility against local Node v24.14.0, and tracked diff whitespace check.
- Not executed here: Java compilation/tests/full build/coverage, isolated npm installation, frontend test/typecheck/lint/format/build, independent review, or deployed HTTP tests.
- Not installed in live Vite: all 15 changed package manifests still show old installed versions. The npm command unexpectedly rewrote `frontend/node_modules/.package-lock.json` metadata; MA was notified. No corrective live installation or metadata repair was performed. A byte-for-byte node_modules preservation claim would be inaccurate.
- No real DB, DDL, provider/mail, credential, media/log cleanup, server restart, Git write, or subdelegation.

## MA Handoff

Owned Java/lock inputs are stable; product/test edits paused for MA's patched Boot build retry. Corrected test SHA-256: `2C7C6ED3D70F1CCD74D40E925E4EC20D5A504D68704E73CF4DC0052935C7E610`. First focused command: `./gradlew.bat test --tests "com.atstudio.atstudio.config.AppConfigMultipartBoundaryTest"`, then the existing full `./gradlew.bat build` and managed dependency inspection. No runner was started by this assignee; the slot is available for WI010 review.

Frontend verification must use MA's isolated `%LOCALAPPDATA%/ATStudio/validation/<unique>` copy, not the live node_modules tree. Keep evidence in `output/release-remediation-20260909`. Exact commands, hashes, side-effect details, and rollback boundaries are in the evidence pack.

The finite boundary is not a new tag business limit or complete resource-protection proof. Larger future forms, deployment/proxy budgets, the running pinned JAR, and actual target readiness remain separate concerns.

## Related Documents

- [Evidence pack](../agent/WI-20260909-ATS-005-evidence-pack.md)
- [Approved REQ](REQ-20260909-ATS-001.md)
- [Assigned handoff](../agent/WI-20260909-ATS-005-handoff.md)
