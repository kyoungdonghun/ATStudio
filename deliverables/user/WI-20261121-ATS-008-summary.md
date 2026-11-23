# WI-20261121-ATS-008 · Two-account integration review

Two invited users signed in locally, received the correct original files and could see only their own history and licenses. Repeated downloads kept the existing license UUID and consumed no additional quota. Changing the catalog through the administrator API blocked a previously authorized original on unpublishing and restored access on republication. Withdrawing an account blocked use of its existing token on history, license detail and original downloads.

The final Spring MVC workflow passed on H2 and again on a disposable local MySQL 8.0.45 instance. A separate Chrome check loaded the final pilot bundle with synthetic API responses. After B signed in through a second tab, both tabs displayed only B's records without a manual reload. The optional nginx routes passed a static check. Loading the native nginx configuration and verifying device downloads are outstanding before inviting users.

[Full evidence and local reproduction](../agent/WI-20261121-ATS-008-evidence-pack.md) · [Independent review requirement](REQ-20261121-ATS-003.md)
