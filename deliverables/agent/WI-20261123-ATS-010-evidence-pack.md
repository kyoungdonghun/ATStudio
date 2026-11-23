# WI-20261123-ATS-010 · B1 frontend release preparation

[Release scope](../user/REQ-20261123-ATS-002.md) and [handoff](WI-20261123-ATS-010-handoff.md).

The local operator entry point is `build/WI-20261123-ATS-010/release.py`. The prior native apply source, previous B1 tar, manifest, JAR and nginx file are identified by SHA-256 in that tool. Its retained-asset comparison covers every fingerprinted asset on the preflight manifest. Any same-name asset with different content stops preparation. Archive members are extracted in a fresh release directory. The sole allowed active-site change replaces the frontend root. The native checks compare the pre- and post-switch application, database and nginx service identities and retain a rollback configuration. A consumed apply claim requires reconciliation.

Local evidence: preparing the retained B1 bundle unchanged was refused; preparing a modified HTML fixture succeeded with 41 candidate files and 126 total manifest entries; injecting same-name/different-content JavaScript was rejected. The generated native adapter compiled. These local checks used temporary directories. They did not contact production or certify release activation.

The owner runs `prepare` on the final reviewed build, `preflight`, `apply`, and `verify` with the same CLI. `rollback` restores only the verified baseline from an owned active release. Operational results belong to the owner. Sensitive site snapshots, assembly tar and private receipts stay outside Git.
