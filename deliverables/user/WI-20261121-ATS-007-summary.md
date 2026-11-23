# B1 participant screen review

The local pilot now keeps a shared browser's account, personal history, licenses and download actions together when users switch accounts in another tab. In-flight downloads from the previous login cannot trigger a file save or quota notification in the new session. A participant can open a public track from personal history without copying the private history list into the shared player.

All 181 selected frontend tests passed, including account replacement during a download and recovery from a login storage failure. Type checking, lint and the pilot build passed. The first visit after switching to pilot mode requires one fresh sign-in. Subsequent pilot tabs share the login.

The running site was outside this review. Backend ownership, integration results and production acceptance are tracked separately. [Evidence and reproduction](../agent/WI-20261121-ATS-007-evidence-pack.md)
