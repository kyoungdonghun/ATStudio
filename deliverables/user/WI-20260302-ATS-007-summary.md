[WI-007 SUMMARY]
Status: COMPLETED
Changes:
- DownloadService.java L20: Added class-level @Transactional(readOnly = true)
- DownloadService.java L47-48: Extracted downloadPerDay variable, added downloadPerDay != -1 guard before limit check
- DownloadServiceTest.java: Added 3 new test cases (unlimited plan zero count, unlimited plan high usage, limited plan under limit)

Issues Fixed:
- C-1 (CRITICAL): downloadPerDay=-1 guard added. Unlimited plan users (downloadPerDay=-1) now bypass the daily limit check entirely, resolving the bug where 0 >= -1 always evaluated true and blocked all downloads.
- M-1 (MAJOR): @Transactional(readOnly=true) added at class level. The mutating download() method already had @Transactional override.

Test Results: BUILD SUCCESSFUL, 9 tests, 0 failures, 0 errors
